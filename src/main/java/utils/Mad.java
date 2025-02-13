package utils;

import java.io.Serializable;

public class Mad implements Serializable {
    public double result;
    public double error_bound;

    public Mad(double result, double error_bound) {
        this.result = result;
        this.error_bound = error_bound;
    }

    public String toString(){
        return result + " - " + error_bound;
    }
}