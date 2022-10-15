package benchmark;

import mad.CORESketch;
import mad.ExactDDSketch;

import java.util.ArrayList;
import java.util.List;

import static utils.FileHelper.GET_SIZE;

public class DD_MAD {
    public static double memory = 0;
    public static double mad;
    public static double median;

    public static double dd_mad(double[] data, double max_value, double min_value, double epsilon,
                                int maxNumBins, boolean is_integer) {
        memory = 0;
        double[] data_exp = new double[data.length];
        System.arraycopy(data, 0, data_exp, 0, data.length);
        median = dd_median(data_exp, max_value, min_value, epsilon, maxNumBins, is_integer);
        min_value = 100000;
        max_value = -100000;
        for (int i = 0; i < data_exp.length; i++) {
            data_exp[i] = Math.abs(data_exp[i] - median + 1);
            min_value = Math.min(min_value, data_exp[i]);
            max_value = Math.max(max_value, data_exp[i]);
        }
        mad = dd_median(data_exp, max_value, min_value, epsilon, maxNumBins, is_integer);
//        System.out.println("DD_SPACE: " + memory);
//        System.out.println(mad);
        return memory;
    }

    public static double dd_median(double[] data, double max_value, double min_value, double epsilon,
                                   int maxNumBins, boolean is_integer) {
        double m = 0;
        double card = epsilon;
        double[] median_range = new double[] {min_value, max_value};
        ExactDDSketch sketch = new ExactDDSketch(card, maxNumBins, median_range);
        while (true) {
            for(double datum: data){
                sketch.insert_ex(datum);
            }
            memory = Math.max(memory, sketch.sketch_size() * 48.13 / 1024);
            if (sketch.data_read()) {
                median_range = sketch.get_range();
                long[] edge_num = sketch.get_gap();
                sketch = new ExactDDSketch();
                List<Double> queue = new ArrayList<>();
                for (double datum : data) {
                    if (sketch.in_range(datum, median_range)) {
                        queue.add(datum);
                    }
                }
                long rank = 0;
                queue.sort(Double::compareTo);
                memory = Math.max(memory, ((double)queue.size()) * 8 / 1024);
                rank += edge_num[0];
                for (Double aDouble : queue) {
                    rank += 1;
                    if (rank >= Math.floor(0.5 * (data.length + 1))) {
                        m = aDouble;
                        break;
                    }
                }
                break;
            }
            int p = sketch.median_bucket();
            if (is_integer && sketch.bucket_finest(p)) {
                m = sketch.median(p);
                break;
            }
            median_range = sketch.generate_useful_range(p);
            memory = Math.max(memory, sketch.sketch_size() * 48.13 / 1024);
            card *= 0.5;
            sketch = new ExactDDSketch(card, maxNumBins, median_range);
        }
        return m;
    }
}