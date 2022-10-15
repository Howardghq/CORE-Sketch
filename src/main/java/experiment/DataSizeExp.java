package experiment;

import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;
import mad.CORESketch;

import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class DataSizeExp {
    public static void main(String[] args) throws IOException {
        // double[] nums = READ("E:/dataset/gas.csv", 296128541, 296128541);
        Random r = new Random();
        int[] sizes = new int[] {10000, 100000, 1000000, 2000000, 5000000, 10000000};
//        int[] sizes = new int[] {10000};
        double mu = 4;
        double sigma = 2;
        double minimum = 100000;
        double maximum = -100000;
        double times = 1;
        double spaces_core;
        double spaces_dd;
        double spaces_exact;
        double times_core;
        double times_dd;
        double times_exact;
        for (int size : sizes) {
            spaces_core = 0;
            spaces_dd = 0;
            spaces_exact = 0;
            times_core = 0;
            times_dd = 0;
            times_exact = 0;
            CORESketch sketch = new CORESketch();
            System.out.println("Data Size: " + size);
            for (int n = 0; n < times; n++) {
                double[] data = READ("/Users/howardguan/Documents/THU/DQ/" +
                                "Query/Space MAD/dataset/gas.csv",
                        size, 10000000);
                for (int i = 0; i < data.length; i++) {
                    // data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
                    // data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }
                double time_exact = System.nanoTime();
                double exact = EXACT_MAD.exact_mad(data, data.length);
                times_exact += (System.nanoTime() - time_exact) / 1000000;
                spaces_exact += exact;
                double time_core = System.nanoTime();
                double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                        1, 7500, false);
                times_core += (System.nanoTime() - time_core) / 1000000;
                spaces_core += core;
                double time_dd = System.nanoTime();
                double dd = DD_MAD.dd_mad(data, maximum - minimum + 1,
                        1, 0.05, 7500, false);
                times_dd += (System.nanoTime() - time_dd) / 1000000;
                spaces_dd += dd;
//                System.out.println("EXACT_TIME: " + time_exact / 1000000);
//                System.out.println("CORE_TIME: " + time_core / 1000000);
//                System.out.println("DD_TIME: " + time_dd / 1000000);
//                System.out.println("EXACT_MAD: " + exact);
//                System.out.println("CORE_MAD: " + core);
//                System.out.println("DD_MAD: " + dd);
                if (size == 10000000) {
                    spaces_exact *= times;
                    spaces_core *= times;
                    spaces_dd *= times;
                    times_exact *= times;
                    times_core *= times;
                    times_dd *= times;
                    break;
                }
            }
            System.out.println("SPACE: " + (spaces_core / times) + " " + (spaces_dd / times) + " " +  (spaces_exact / times));
            System.out.println("TIME: " + (times_core / times) + " " + (times_dd / times) + " " + (times_exact / times));
//            System.out.println("SPACE: " + (spaces_core / times) + " " +  (spaces_exact / times));
//            System.out.println("TIME: " + (times_core / times) + " " + (times_exact / times));
        }
    }
}
