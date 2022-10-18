package benchmark;

import java.util.*;
import static utils.FileHelper.*;

public class EXACT_MAD {
    public static double memory = 0;
    public static double median = 0;


    public static double exact_mad(double[] data, int des) {
        memory = 0;

        List<Double> queue = new ArrayList<>();

        for(int i = 0; i < des; i++){
            queue.add(data[i]);
        }

        median = query(queue);
//        System.out.println(median);

        queue.clear();
        for(int i = 0; i < des; i++){
            queue.add(Math.abs(data[i] - median));
        }

        double mad = query(queue);
        memory = (double)data.length * 8 / 1024;
//        System.out.println(mad);
        return memory;
    }

    public static double query(List<Double> data){
        data.sort(Double::compareTo);
        int rank = (int) Math.floor(0.5 * (data.size() - 1));
        return data.get(rank);
    }
}
