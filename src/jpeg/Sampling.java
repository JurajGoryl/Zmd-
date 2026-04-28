package jpeg;

import Jama.Matrix;
import enums.SamplingType;

public class Sampling {
    public static Matrix sampleDown(Matrix inputMatrix, SamplingType samplingType) {
        switch (samplingType) {
            case S_4_4_4:
                return inputMatrix.copy();
            case S_4_2_2:
                return sampleHorizontalDown(inputMatrix, 2);
            case S_4_1_1:
                return sampleHorizontalDown(inputMatrix, 4);
            case S_4_2_0:
                Matrix horizontalDown = sampleHorizontalDown(inputMatrix, 2);
                return sampleHorizontalDown(horizontalDown.transpose(), 2).transpose();
            default:
                return inputMatrix;
        }
    }

    public static Matrix sampleUp(Matrix inputMatrix, SamplingType samplingType) {
        switch (samplingType) {
            case S_4_4_4:
                return inputMatrix.copy();
            case S_4_2_2:
                return sampleHorizontalUp(inputMatrix, 2);
            case S_4_1_1:
                return sampleHorizontalUp(inputMatrix, 4);
            case S_4_2_0:
                Matrix horizontalUp = sampleHorizontalUp(inputMatrix, 2);
                return sampleHorizontalUp(horizontalUp.transpose(), 2).transpose();
            default:
                return inputMatrix;
        }
    }

    private static Matrix sampleHorizontalDown(Matrix mat, int step) {
        int h = mat.getRowDimension();
        int w = mat.getColumnDimension();
        int newW = w / step;

        Matrix result = new Matrix(h, newW);

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < newW; c++) {
                result.set(r, c, mat.get(r, c * step));
            }
        }
        return result;
    }

    private static Matrix sampleHorizontalUp(Matrix mat, int step) {
        int h = mat.getRowDimension();
        int w = mat.getColumnDimension();
        int newW = w * step;

        Matrix result = new Matrix(h, newW);

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double val = mat.get(r, c);
                for (int k = 0; k < step; k++) {
                    result.set(r, (c * step) + k, val);
                }
            }
        }
        return result;
    }
}
