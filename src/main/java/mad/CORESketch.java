package mad;

import java.io.Serializable;
import java.util.*;

import static utils.FileHelper.GET_SIZE;
import static utils.Params.*;

public class CORESketch implements Serializable {
    private int card;
    private int max_bucket;
    private Map<Long, Long> buckets;
    transient private double[] useful_range;
    private long data_size;
    private long pruned_size;
    private long left_count;
    private long right_count;

    transient private long[] mid_num;
    transient private Bucket[] bucket_sequence;

    private static class Bucket {
        long index;
        long count;
        double lower_bound;
        double upper_bound;
        boolean finest;

        Bucket(long index, long count, double lower_bound, double upper_bound, boolean finest) {
            this.index = index;
            this.count = count;
            this.lower_bound = lower_bound;
            this.upper_bound = upper_bound;
            this.finest = finest;
        }
    }

    public CORESketch() {
        this.card = 0;
        this.max_bucket = SPACE_LIMIT;
        this.useful_range = new double[6];
        this.buckets = new HashMap<>((int) (this.max_bucket));
        this.mid_num = new long[4];
        this.bucket_sequence = new Bucket[(int) max_bucket];
        this.left_count = 0;
        this.right_count = 0;
    }

    public CORESketch(int card, int max_bucket, double[] useful_range) {
        this.card = card;
        this.max_bucket = max_bucket;
        this.buckets = new HashMap<>(this.max_bucket);
        this.useful_range = new double[6];
        System.arraycopy(useful_range, 0, this.useful_range, 0, 6);
        this.mid_num = new long[4];
        this.bucket_sequence = new Bucket[(int) max_bucket];
        this.left_count = 0;
        this.right_count = 0;
    }

    public boolean in_range(double v, double[] range) {
        return (v > range[0] && v < range[1]) ||
                (v >= range[2] && v < range[3]) ||
                (v > range[4] && v < range[5]);
    }

    public void insert_mid(double v) {
        if (v <= useful_range[0]) {
            mid_num[0] += 1;
        }
        else if (v >= useful_range[1] && v < useful_range[2]) {
            mid_num[1] += 1;
        }
        else if (v >= useful_range[3] && v <= useful_range[4]) {
            mid_num[2] += 1;
        }
        else if (v >= useful_range[5]) {
            mid_num[3] += 1;
        }
    }

    public double calculate_maximum_space() {
        for (long i = 0; i < max_bucket; i++) {
            buckets.put(i, 1000000 + i);
        }
        serialize_bucket();
        System.out.println(GET_SIZE(buckets));
        return GET_SIZE(this);
    }

    public double customize(double v) {
        if (v < useful_range[0]) {
            return useful_range[0];
        } else if (v > useful_range[1] && v < useful_range[2]) {
            return useful_range[2];
        } else if (v > useful_range[3] && v < useful_range[4]) {
            return useful_range[4];
        } else if (v > useful_range[5]) {
            return useful_range[5];
        }
        return v;
    }

    public void insert(double v) {
        data_size += 1;
        if (!in_range(v, useful_range)) {
            pruned_size += 1;
            insert_mid(v);
            v = customize(v);
        }
        long i = (long) Math.ceil(Math.pow(2, this.card) * Math.log(v) / Math.log(2));
        buckets.put(i, buckets.getOrDefault(i, 0L) + 1);
    }

    public void serialize_bucket() {
        int i = 0;
        double zeta = Math.pow(2, Math.pow(2, -card));
        bucket_sequence = new Bucket[buckets.size()];
        for (Map.Entry<Long, Long> e : buckets.entrySet()) {
            double lb = Math.pow(zeta, e.getKey() - 1);
            double ub = Math.pow(zeta, e.getKey());
            boolean finest = Math.ceil(lb) == Math.floor(ub);
            bucket_sequence[i++] = new Bucket(e.getKey(), e.getValue(), lb, ub, finest);
        }
        Arrays.sort(bucket_sequence, Comparator.comparingLong(o -> o.index));
    }

    public int mid_half_count_bucket() {
        serialize_bucket();
        long count = 0;
        double rank = 0.5 * (data_size - 1);
        for (int m = 0; m < bucket_sequence.length; ++m) {
            count += bucket_sequence[m].count;
            if (count > rank) {
                return m;
            }
        }
        return -1;
    }

    public double[] real_range() {
        double[] real_range = new double[7];
        real_range[6] = 3;
        real_range[0] = useful_range[0];
        real_range[1] = useful_range[1];
        int cursor = 2;
        if (useful_range[2] < real_range[cursor - 1]) {
            real_range[cursor - 1] = Math.max(useful_range[3], real_range[cursor - 1]);
            real_range[6] -= 1;
        } else {
            real_range[cursor] = useful_range[2];
            real_range[cursor + 1] = useful_range[3];
            cursor += 2;
        }
        if (useful_range[4] < real_range[cursor - 1]) {
            real_range[cursor - 1] = Math.max(useful_range[5], real_range[cursor - 1]);
            real_range[6] -= 1;
        } else {
            real_range[cursor] = useful_range[4];
            real_range[cursor + 1] = useful_range[5];
        }
        return real_range;
    }

    public int[] edge_half_count_bucket(int m) {
        long count = bucket_sequence[m].count;
        int cursor = m;
        double rank = 0.5 * data_size;
        int l = m - 1;
        int r = m + 1;
        left_count += bucket_sequence[m].count;
        right_count += bucket_sequence[m].count;
        while (count <= rank && l >= 0 && r < bucket_sequence.length) {
            if (bucket_sequence[m].lower_bound - bucket_sequence[l].upper_bound <
                    bucket_sequence[r].lower_bound - bucket_sequence[m].upper_bound) {
                cursor = l--;
                left_count += bucket_sequence[cursor].count;
            } else {
                cursor = r++;
                right_count += bucket_sequence[cursor].count;
            }
            count += bucket_sequence[cursor].count;
        }

        while (count <= rank && l >= 0) {
            cursor = l--;
            left_count += bucket_sequence[cursor].count;
            count += bucket_sequence[cursor].count;
        }

        while (count <= rank && r < bucket_sequence.length) {
            cursor = r++;
            right_count += bucket_sequence[cursor].count;
            count += bucket_sequence[cursor].count;
        }
        r -= 1;
        l += 1;
        if (r == bucket_sequence.length - 1 && cursor != r) r = -1;
        if (l == 0 && cursor != l) l = -1;
        return new int[] {l, r};
    }

    public double[] generate_useful_range(int m, int l, int r) {
        double[] new_range = new double[6];
        if (l == -1) {
            new_range[0] = Math.max(useful_range[0],
                    2 * bucket_sequence[m].lower_bound - bucket_sequence[r].upper_bound);
            new_range[1] = Math.min(useful_range[1],
                    2 * bucket_sequence[m].upper_bound - bucket_sequence[r].lower_bound);
            new_range[2] = Math.max(useful_range[2], bucket_sequence[m].lower_bound);
            new_range[3] = Math.min(useful_range[3], bucket_sequence[m].upper_bound);
            new_range[4] = Math.max(useful_range[4],
                    bucket_sequence[m].lower_bound + bucket_sequence[r].lower_bound - bucket_sequence[m].upper_bound);
            new_range[5] = Math.min(useful_range[5],
                    bucket_sequence[m].upper_bound + bucket_sequence[r].upper_bound - bucket_sequence[m].lower_bound);
        }
        if (r == -1) {
            new_range[0] = Math.max(useful_range[0],
                    bucket_sequence[m].lower_bound - bucket_sequence[m].upper_bound + bucket_sequence[l].lower_bound);
            new_range[1] = Math.min(useful_range[1],
                    bucket_sequence[m].upper_bound - bucket_sequence[m].lower_bound + bucket_sequence[l].upper_bound);
            new_range[2] = Math.max(useful_range[2], bucket_sequence[m].lower_bound);
            new_range[3] = Math.min(useful_range[3], bucket_sequence[m].upper_bound);
            new_range[4] = Math.max(useful_range[4],
                    2 * bucket_sequence[m].lower_bound - bucket_sequence[l].upper_bound);
            new_range[5] = Math.min(useful_range[5],
                    2 * bucket_sequence[m].upper_bound - bucket_sequence[l].lower_bound);
        }
        new_range[0] = Math.max(useful_range[0], Math.min(
            bucket_sequence[m].lower_bound - bucket_sequence[m].upper_bound + bucket_sequence[l].lower_bound, 
            2 * bucket_sequence[m].lower_bound - bucket_sequence[r].upper_bound));
        new_range[1] = Math.min(useful_range[1], Math.max(
            bucket_sequence[m].upper_bound - bucket_sequence[m].lower_bound + bucket_sequence[l].upper_bound, 
            2 * bucket_sequence[m].upper_bound - bucket_sequence[r].lower_bound));
        new_range[2] = Math.max(useful_range[2], bucket_sequence[m].lower_bound);
        new_range[3] = Math.min(useful_range[3], bucket_sequence[m].upper_bound);
        new_range[4] = Math.max(useful_range[4], Math.min(
            2 * bucket_sequence[m].lower_bound - bucket_sequence[l].upper_bound, 
            bucket_sequence[m].lower_bound + bucket_sequence[r].lower_bound - bucket_sequence[m].upper_bound));
        new_range[5] = Math.min(useful_range[5], Math.max(
            2 * bucket_sequence[m].upper_bound - bucket_sequence[l].lower_bound, 
            bucket_sequence[m].upper_bound + bucket_sequence[r].upper_bound - bucket_sequence[m].lower_bound));
        return new_range;
    }

    public boolean bucket_finest(int m, int l, int r) {
        return bucket_sequence[m].finest && bucket_sequence[l].finest;
    }

    public double mad(int m, int l, int r) {
        double choose_right = right_count + left_count - bucket_sequence[m].count - bucket_sequence[l].count;
        double choose_left = left_count + right_count - bucket_sequence[m].count - bucket_sequence[r].count;
        if (choose_right < 0.5 * data_size && choose_left >= 0.5 * data_size) {
            return Math.floor(bucket_sequence[m].upper_bound) - Math.floor(bucket_sequence[l].upper_bound);
        }
        else if (choose_right >= 0.5 * data_size && choose_left < 0.5 * data_size) {
            return Math.floor(bucket_sequence[r].upper_bound) - Math.floor(bucket_sequence[m].upper_bound);
        }
        else if (choose_right < 0.5 * data_size && choose_left < 0.5 * data_size) {
            return Math.max(Math.floor(bucket_sequence[m].upper_bound) -
                            Math.floor(bucket_sequence[l].upper_bound),
                    Math.floor(bucket_sequence[r].upper_bound) -
                            Math.floor(bucket_sequence[m].upper_bound));
        }
        else {
            return Math.min(Math.floor(bucket_sequence[m].upper_bound) -
                            Math.floor(bucket_sequence[l].upper_bound),
                    Math.floor(bucket_sequence[r].upper_bound) -
                            Math.floor(bucket_sequence[m].upper_bound));
        }
    }

    public long useful_count() {
        return data_size - pruned_size;
    }

    public int get_bucket_size() {
        return buckets.size();
    }

    public boolean data_read() {
        return useful_count() * 8 <= max_bucket * (32.125 + 16);
    }

    public double[] get_range() {
        return useful_range;
    }

    public long[] get_gap() {return mid_num;}

    public void set_card(int card) {
        this.card = card;
    }

}
