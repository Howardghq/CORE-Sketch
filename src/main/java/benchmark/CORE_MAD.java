package benchmark;

import mad.CORESketch;

import java.util.ArrayList;
import java.util.List;
import static utils.FileHelper.*;


public class CORE_MAD {
    public static double memory = 0;
    public static double mad;
    public static double median;

    private static int calculate_card(double max_value, double min_value, int space_limit) {
        int card = 0;
        while (true) {
            if (Math.ceil(Math.pow(2, card) * Math.log(max_value) / Math.log(2)) -
                    Math.floor(Math.pow(2, card) * Math.log(min_value) / Math.log(2))
                    > space_limit) {
                return card - 1;
            }
            card += 1;
        }
    }

    private static int calculate_card(double[] real_range, int space_limit) {
        int card = 0;
        while (true) {
            int space_expected = 0;
            for (int i = 0; i < real_range[6]; ++i) {
                space_expected += Math.ceil(Math.pow(2, card) * Math.log(real_range[2 * i + 1]) / Math.log(2)) -
                Math.floor(Math.pow(2, card) * Math.log(real_range[2 * i]) / Math.log(2));
            }
            if (space_expected > space_limit) {
                return card - 1;
            }
            card += 1;
        }
    }

    public static double core_mad(double[] data, double max_value, double min_value, int space_limit, boolean is_integer) {
        memory = 0;
        int card = calculate_card(max_value, min_value, space_limit);
        double[] useful_range = new double[] {min_value, max_value,
                min_value, max_value, min_value, max_value};
        CORESketch sketch = new CORESketch(card, space_limit, useful_range);
        while (true) {
            for (double datum : data) {
                sketch.insert(datum);
            }
            memory = Math.max(memory, sketch.get_bucket_size() * 48.13 / 1024);
            if (sketch.data_read()) {
                useful_range = sketch.get_range();
                long[] mid_num = sketch.get_gap();
                sketch = new CORESketch();
                List<Double> queue = new ArrayList<>();
                long rank = 0;
                for (double datum : data){
                    if (sketch.in_range(datum, useful_range)) {
                        queue.add(datum);
                    }
                }
                queue.sort(Double::compareTo);
                memory = Math.max(memory, ((double)queue.size()) * 8 / 1024);
                rank += mid_num[0];
                for (int i = 0; i < queue.size(); i++) {
                    rank += 1;
                    if (i > 0 && queue.get(i - 1) < useful_range[1] && queue.get(i) > useful_range[2]) {
                        rank += mid_num[1];
                    }
                    if (rank >= Math.floor(0.5 * (data.length + 1))) {
                        median = queue.get(i);
                        break;
                    }
                }
                queue.replaceAll(aDouble -> Math.abs(aDouble - median));
                queue.sort(Double::compareTo);
                rank = 0;
                double min_mad = Math.max(median - useful_range[1], useful_range[4] - median);
                for (int i = 0; i < queue.size(); i++) {
                    rank += 1;
                    if (i > 0 && queue.get(i) > min_mad && queue.get(i - 1) <= min_mad) {
                        rank += mid_num[1] + mid_num[2];
                    }
                    if (rank >= Math.floor(0.5 * (data.length + 1))) {
                        mad = queue.get(i);
//                        System.out.println("CORE_SPACE: " + memory);
//                        System.out.println(mad);
                        return memory;
                    }
                }
            }
            int m = sketch.mid_half_count_bucket();
            int[] lr = sketch.edge_half_count_bucket(m);
            if (is_integer && sketch.bucket_finest(m, lr[0], lr[1])) {
//                System.out.println("CORE_SPACE: " + memory);
//                return sketch.mad(m, lr[0], lr[1]);
//                System.out.println(sketch.mad(m, lr[0], lr[1]));
                return memory;
            }
            double[] next_range = sketch.generate_useful_range(m, lr[0], lr[1]);
            memory = Math.max(sketch.get_bucket_size() * 48.13 / 1024, memory);
            sketch = new CORESketch(0, space_limit, next_range);
            double[] real_range = sketch.real_range();
            card = calculate_card(real_range, space_limit);
            sketch.set_card(card);
        }
    }
}
