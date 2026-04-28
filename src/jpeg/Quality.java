package jpeg;

import Jama.Matrix;

public class Quality {

    public static double[][] convertIntToDouble(int[][] intArray) {
        double[][] doubleArray = new double[intArray.length][intArray[0].length];
        for (int i = 0; i < intArray.length; i++) {
            for (int j = 0; j < intArray[0].length; j++) {
                doubleArray[i][j] = (double) intArray[i][j];
            }
        }
        return doubleArray;
    }

    public static double countMSE(double[][] original, double[][] modified) {
        double mse = 0.0;
        int height = original.length;
        int width = original[0].length;

        for (int m = 0; m < height; m++) {
            for (int n = 0; n < width; n++) {
                mse += Math.pow(original[m][n] - modified[m][n], 2);
            }
        }
        return mse / (width * height);
    }

    public static double countMAE(double[][] original, double[][] modified) {
        double mae = 0.0;
        int height = original.length;
        int width = original[0].length;

        for (int m = 0; m < height; m++) {
            for (int n = 0; n < width; n++) {
                mae += Math.abs(original[m][n] - modified[m][n]);
            }
        }
        return mae / (width * height);
    }

    public static double countSAE(double[][] original, double[][] modified) {
        double sae = 0.0;
        int height = original.length;
        int width = original[0].length;

        for (int m = 0; m < height; m++) {
            for (int n = 0; n < width; n++) {
                sae += Math.abs(original[m][n] - modified[m][n]);
            }
        }
        return sae;
    }

    public static double countPSNR(double mse) {
        double psnr = 10 * Math.log10(Math.pow(255, 2) / mse);
        return psnr;
    }

    public static double countPSNRforRGB(double mseRed, double mseGreen, double mseBlue) {
        double averageMse = (mseRed + mseGreen + mseBlue) / 3.0;
        return countPSNR(averageMse);
    }

    public static double countSSIM(Matrix original, Matrix modified) {
        throw new RuntimeException("Not implemented yet.");
    }

    public static double countMSSIM(Matrix original, Matrix modified) {
        throw new RuntimeException("Not implemented yet.");
    }
}