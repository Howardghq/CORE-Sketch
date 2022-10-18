import benchmark.DD_MAD;
import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;

import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class Main {
    public static void main(String[] args) throws IOException {
        double[] data = READ("/Users/howardguan/Documents/THU/DQ/" +
                        "Query/Space MAD/dataset/bitcoin.csv",
                10000000, 10000000);
//        Random r = new Random();
        // double[] data = new double[10000000];
        // double mu = 4;
        // double sigma = 2;
        double minimum = 100000;
        double maximum = -100000;
        for (int i = 0; i < data.length; i++) {
            // data[i] = Math.pow(sigma, 2) * r.nextGaussian() + mu;
            // data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
            if (data[i] < minimum) minimum = data[i];
            if (data[i] > maximum) maximum = data[i];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] - minimum + 1;
        }
        System.out.println(minimum - 1);
        double time_exact = System.nanoTime();
        double exact = EXACT_MAD.exact_mad(data, data.length);
        time_exact = System.nanoTime() - time_exact;
//        double time_core = System.nanoTime();
//        double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
//                1, 500, true);
//        time_core = System.nanoTime() - time_core;
//        double time_dd = System.nanoTime();
//        double dd = DD_MAD.dd_mad(data, maximum - minimum + 1,
//                1, 0.1, 500, false);
//        time_dd = System.nanoTime() - time_dd;
//        System.out.println("EXACT_TIME: " + time_exact);
//        System.out.println("CORE_TIME: " + time_core);
//        System.out.println("DD_TIME: " + time_dd);
//        System.out.println("EXACT_MAD: " + exact);
//        System.out.println("CORE_MAD: " + core);
//        System.out.println("DD_MAD: " + dd);
    }
}