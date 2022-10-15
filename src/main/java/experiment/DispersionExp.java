package experiment;

import benchmark.EXACT_MAD;
import benchmark.CORE_MAD;

import java.util.Random;
import java.io.IOException;

import static utils.FileHelper.READ;

public class DispersionExp {
    public static void main(String[] args) throws IOException {
        Random r = new Random();
        double[] stds = new double[] {0.5, 2, 5, 8, 10};
        int[] sizes = new int[] {10000, 100000, 1000000, 2000000, 5000000, 10000000};
        double mu = 4;
        double minimum = 100000;
        double maximum = -100000;
        double[][] space = new double[6][5];
        int index = 0;
        for (double std : stds) {
            System.out.println("SD: " + std);
            for (int j = 0; j < sizes.length; j++) {
                System.out.println("Data Size: " + sizes[j]);
                double[] data = new double[sizes[j]];
                for (int i = 0; i < sizes[j]; i++) {
                    data[i] = Math.pow(std, 2) * r.nextGaussian() + mu;
                    //                data[i] = Math.floor(Math.pow(sigma, 2) * r.nextGaussian() + mu);
                    if (data[i] < minimum) minimum = data[i];
                    if (data[i] > maximum) maximum = data[i];
                }
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i] - minimum + 1;
                }
                //            double[] data = new double[100000];
                double time_core = System.nanoTime();
                double core = CORE_MAD.core_mad(data, maximum - minimum + 1,
                        1, 100, false);
                time_core = System.nanoTime() - time_core;
//                System.out.println("CORE_TIME: " + time_core / 1000000);
//                System.out.println("CORE_SPACE: " + core);
                space[j][index] = time_core / 1000000;
            }
            index += 1;
        }
        System.out.println(space[0][0] + " " + space[0][1] + " " + space[0][2] + " " + space[0][3] + " " + space[0][4]);
        System.out.println(space[1][0] + " " + space[1][1] + " " + space[1][2] + " " + space[1][3] + " " + space[1][4]);
        System.out.println(space[2][0] + " " + space[2][1] + " " + space[2][2] + " " + space[2][3] + " " + space[2][4]);
        System.out.println(space[3][0] + " " + space[3][1] + " " + space[3][2] + " " + space[3][3] + " " + space[3][4]);
        System.out.println(space[4][0] + " " + space[4][1] + " " + space[4][2] + " " + space[4][3] + " " + space[4][4]);
        System.out.println(space[5][0] + " " + space[5][1] + " " + space[5][2] + " " + space[5][3] + " " + space[5][4]);
    }
}
