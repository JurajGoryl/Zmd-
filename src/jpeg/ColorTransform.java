package jpeg;

import Jama.Matrix;

public class ColorTransform {

    // RGB → YCbCr
    public static Matrix[] convertOriginalRGBtoYcBcR(int[][] red, int[][] green, int[][] blue) {

        int height = red.length;
        int width = red[0].length;

        Matrix convertedY = new Matrix(height, width);
        Matrix convertedCb = new Matrix(height, width);
        Matrix convertedCr = new Matrix(height, width);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int R = red[i][j];
                int G = green[i][j];
                int B = blue[i][j];

                double Y  = 0.257 * R + 0.504 * G + 0.098 * B + 16;
                double Cb = -0.148 * R - 0.291 * G + 0.439 * B + 128;
                double Cr = 0.439 * R - 0.368 * G - 0.071 * B + 128;

                convertedY.set(i, j, Y);
                convertedCb.set(i, j, Cb);
                convertedCr.set(i, j, Cr);
            }
        }

        return new Matrix[]{convertedY, convertedCb, convertedCr};
    }

    // YCbCr → RGB
    public static int[][][] convertModifiedYcBcRtoRGB(Matrix Y, Matrix Cb, Matrix Cr) {

        int height = Y.getRowDimension();
        int width = Y.getColumnDimension();

        int[][] convertedRed = new int[height][width];
        int[][] convertedGreen = new int[height][width];
        int[][] convertedBlue = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                double y = Y.get(i, j);
                double cb = Cb.get(i, j);
                double cr = Cr.get(i, j);

                int R = (int) Math.round(1.164 * (y - 16) + 1.596 * (cr - 128));
                int G = (int) Math.round(1.164 * (y - 16) - 0.813 * (cr - 128) - 0.391 * (cb - 128));
                int B = (int) Math.round(1.164 * (y - 16) + 2.018 * (cb - 128));

                R = Math.max(0, Math.min(255, R));
                G = Math.max(0, Math.min(255, G));
                B = Math.max(0, Math.min(255, B));

                convertedRed[i][j] = R;
                convertedGreen[i][j] = G;
                convertedBlue[i][j] = B;
            }
        }

        return new int[][][]{convertedRed, convertedGreen, convertedBlue};
    }
}
