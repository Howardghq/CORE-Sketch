package mad;

import utils.Mad;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.math3.util.Precision.EPSILON;
import static utils.Params.*;

public class ExactDDSketch implements Serializable {
    private double alpha;
    private double gamma;
    private double multiplier;
    private int bucket_num_limit;
    private int threshold_for_compression;

    private Map<Long, Long> positive_buckets;
    private Map<Long, Long> negative_buckets;

    transient private Bucket[] bucket_sequence;
    private double collapse_bound;
    private long zero_count;
    private long data_size;
    private long pruned_size;

    transient private double beta;
    transient private final double[] valid_range;
    transient private long[] edge_num;
    transient private double[] median_range;

    private static double MIN_POSITIVE_VALUE = FS_MIN_POSITIVE_VALUE;
    private static double COEFFICIENT = FS_COMPRESSION_COEFFICIENT;

    public ExactDDSketch() {
        this(EPSILON, BUCKET_NUM_LIMIT, new double[2]);
    }

    public ExactDDSketch(double alpha, int bucket_num_limit, double[] median_range) {
//        System.out.println(alpha);
        this.alpha = alpha;
        this.bucket_num_limit = Math.max(bucket_num_limit, 2);
        this.threshold_for_compression = (int) (bucket_num_limit * COEFFICIENT);

        this.gamma = 2 * alpha / (1 - alpha) + 1;
        this.multiplier = Math.log(Math.E) / (Math.log1p(gamma - 1));
        this.beta = 1;
        this.positive_buckets = new HashMap<>((int) (bucket_num_limit * 0.75));
        this.negative_buckets = new HashMap<>((int) (bucket_num_limit * 0.25));
        this.zero_count = 0;
        this.collapse_bound = -Double.MAX_VALUE;
        this.valid_range = new double[6];
        this.data_size = 0;
        this.pruned_size = 0;
        this.median_range = median_range;
        this.edge_num = new long[]{0, 0};
        this.bucket_sequence = new ExactDDSketch.Bucket[(int) bucket_num_limit];
    }

    public void insert(double v) {
        if (v < collapse_bound) {
            v = collapse_bound;
        }
        if (v > MIN_POSITIVE_VALUE) {
            long i = (long) Math.ceil(Math.log(v) * multiplier);
            positive_buckets.put(i, positive_buckets.getOrDefault(i, 0L) + 1);
        } else if (v < -MIN_POSITIVE_VALUE) {
            long i = (long) Math.ceil(Math.log(-v) * multiplier);
            negative_buckets.put(i, negative_buckets.getOrDefault(i, 0L) + 1);
        } else {
            zero_count++;
        }
        collapse(threshold_for_compression);
    }

    public void insert_ex(double v) {
        data_size += 1;
        if (v < median_range[0]) {
            pruned_size += 1;
            edge_num[0] += 1;
            v = median_range[0];
        }
        if (v > median_range[1]) {
            pruned_size += 1;
            edge_num[1] += 1;
            v = median_range[1];
        }
        insert(v);
    }

    private void collapse(int limit) {
        if (sketch_size() > limit) {
            int exceed = sketch_size() - bucket_num_limit;
            Long[] indices = negative_buckets.keySet().toArray(new Long[0]);
            Arrays.sort(indices);
            long count = 0;
            for (int i = Math.max(0, indices.length - exceed); i < indices.length; ++i) {
                count += negative_buckets.remove(indices[i]);
            }
            if (count > 0) {
                int i = indices.length - exceed - 1;
                if (i >= 0) {
                    negative_buckets.put(indices[i], negative_buckets.get(indices[i]) + count);
                    collapse_bound = -Math.pow(gamma, indices[i]);
                } else {
                    zero_count += count;
                    collapse_bound = 0;
                }
            }
            exceed -= (indices.length - Math.max(0, indices.length - exceed));
            if (exceed > 0) {
                count = zero_count;
                if (zero_count > 0) {
                    exceed--;
                }
                indices = positive_buckets.keySet().toArray(new Long[0]);
                Arrays.sort(indices);
                for (int i = exceed - 1; i >= 0; --i) {
                    count += positive_buckets.remove(indices[i]);
                }
                positive_buckets.put(indices[exceed], positive_buckets.get(indices[exceed]) + count);
                collapse_bound = Math.pow(gamma, indices[exceed] - 1);
            }
        }
    }

    private Bucket[] serialize_bucket() {
        Bucket[] buckets = new Bucket[sketch_size()];
        int i = 0;
        for (Map.Entry<Long, Long> e : positive_buckets.entrySet()) {
            double lb = Math.pow(gamma, e.getKey() - 1);
            double ub = Math.pow(gamma, e.getKey());
            boolean finest = Math.ceil(lb) == Math.floor(ub);
            buckets[i++] = new Bucket(e.getKey(), lb, ub, e.getValue(), finest);
        }
        for (Map.Entry<Long, Long> e : negative_buckets.entrySet()) {
            double lb = -Math.pow(gamma, e.getKey());
            double ub = -Math.pow(gamma, e.getKey() - 1);
            boolean finest = Math.ceil(lb) == Math.floor(ub);
            buckets[i++] = new Bucket(e.getKey(), lb, ub, e.getValue(), finest);
        }
        if (zero_count > 0) {
            buckets[i] = new Bucket(0, 0, 0, zero_count, true);
        }
        Arrays.sort(buckets, Comparator.comparingDouble(o -> o.lower_bound));
        return buckets;
    }

    private long total_count() {
        return positive_buckets.values().stream().mapToLong(l -> l).sum() + negative_buckets.values().stream().mapToLong(l -> l).sum() + zero_count;
    }

    private int find_p_index(Bucket[] buckets, long total_count) {
        long count = 0;
        double rank = 0.5 * (total_count - 1);
        for (int i = 0; i < buckets.length; ++i) {
            count += buckets[i].count;
            if (count > rank) {
                return i;
            }
        }
        return -1;
    }

    public int median_bucket() {
        bucket_sequence = serialize_bucket();
        return find_p_index(bucket_sequence, data_size);
    }

    public double median(int p) {
        return Math.ceil(bucket_sequence[p].lower_bound);
    }

    public double[] generate_useful_range(int p) {
        return new double[]{bucket_sequence[p].lower_bound, bucket_sequence[p].upper_bound};
    }

    public int sketch_size() {
        return positive_buckets.size() + negative_buckets.size() + (zero_count == 0 ? 0 : 1);
    }

    public long useful_count() {
        return data_size - pruned_size;
    }

    public boolean data_read() {
        return useful_count() * 8 <= bucket_num_limit * (32.125 + 16);
    }

    public boolean bucket_finest(int p) {
        return bucket_sequence[p].finest;
    }

    public double[] get_range() {
        return median_range;
    }

    public long[] get_gap() {
        return edge_num;
    }

    public boolean in_range(double datum, double[] median_range) {
        return datum >= median_range[0] && datum <= median_range[1];
    }

    private static class Bucket {
        long index;
        double lower_bound;
        double upper_bound;
        long count;
        boolean finest;

        Bucket(long index, double lower_bound, double upper_bound, long count, boolean finest) {
            this.index = index;
            this.lower_bound = lower_bound;
            this.upper_bound = upper_bound;
            this.count = count;
            this.finest = finest;
        }
    }
}
