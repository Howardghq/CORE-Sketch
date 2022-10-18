package dataset;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.io.*;

public class synthetic {

    public static void main(String[] args){
//        generateUniformData(10000000, 1);
//        generateDiscreteNormData(10000000, 1);
//        generateNormData(10000000, 1);
//        generateParetoData(10000000, 1);
//        generateDiscreteUniformData(10000000, 1);
        generateDirtyData(10000000);
//        generateChiSquareData(10000000, 1);
    }

    public static void generateChiSquareData(int n, int q) {
        FileWriter fw;
        ChiSquaredDistribution chi = new ChiSquaredDistribution(1);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/chi" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(chi.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateDirtyData(int n) {
        FileWriter fw;
        NormalDistribution norm = new NormalDistribution(2, 0.05);
        UniformRealDistribution uni = new UniformRealDistribution(0, 0.1);
        BinomialDistribution bin = new BinomialDistribution(1, 0.1);
        UniformRealDistribution uni1 = new UniformRealDistribution(0, 1);
        NormalDistribution noise = new NormalDistribution(0.4, 0.1);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/dirty.csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                double v = uni.sample() + uni.sample() + 1.9;
                if (bin.sample() == 1) {
                    if (uni1.sample() <= 0.5) {
                        v += noise.sample();
                    }
                    else {
                        v -= noise.sample();
                    }
                }
                fw.write(v + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateDiscreteUniformData(int n, int q) {
        FileWriter fw;
        UniformRealDistribution uni = new UniformRealDistribution(1, 10);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/disc_uni" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(Math.floor(uni.sample()) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateUniformData(int n, int q) {
        FileWriter fw;
        UniformRealDistribution uni = new UniformRealDistribution(1, 10);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/uni" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(uni.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateDiscreteNormData(int n, int q) {
        FileWriter fw;
        NormalDistribution norm = new NormalDistribution(4, 2);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/disc_norm" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(Math.floor(norm.sample()) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateNormData(int n, int q) {
        FileWriter fw;
        NormalDistribution norm = new NormalDistribution(4, 2);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/norm" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(norm.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateDiscreteParetoData(int n, int q) {
        FileWriter fw;
        ParetoDistribution pareto = new ParetoDistribution(1, 1);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/disc_pa" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(Math.floor(pareto.sample()) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateParetoData(int n, int q) {
        FileWriter fw;
        ParetoDistribution pareto = new ParetoDistribution(1, 1);
        try {
            fw = new FileWriter("/Users/howardguan/Documents/THU/DQ/Query/Space MAD/dataset/pareto" + q + ".csv");
            fw.write("value\n");
            for(long i = 0; i < n; ++i){
                fw.write(pareto.sample() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
