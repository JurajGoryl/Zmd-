package jpeg;

import Jama.Matrix;

public class Quantization {
    /**Pro jasovou slozku */
    public static final double[][] quantizationMatrix8Y = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}
    };

    /**Pro barvonosne slozky */
    public static final double[][] quantizationMatrix8C = {
            {17, 18, 24, 47, 99, 99, 99, 99},
            {18, 21, 26, 66, 99, 99, 99, 99},
            {24, 26, 56, 99, 99, 99, 99, 99},
            {47, 66, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99}
    };

    public static Matrix quantize(Matrix input, int blockSize, double quality, boolean matrixY) {
        Matrix qMatrix = Quantization.getQuantizationMatrix(blockSize, quality, matrixY);
        Matrix output = new Matrix(input.getRowDimension(), input.getColumnDimension());

        for (int row = 0; row < input.getRowDimension(); row += blockSize) {
            for (int col = 0; col < input.getColumnDimension(); col += blockSize) {

                Matrix sub = input.getMatrix(row, row + blockSize - 1, col, col + blockSize - 1);
                Matrix quantizedSub = new Matrix(blockSize, blockSize);

                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        double val = sub.get(i, j) / qMatrix.get(i, j);
                        quantizedSub.set(i, j, customRound(val));
                    }
                }
                output.setMatrix(row, row + blockSize - 1, col, col + blockSize - 1, quantizedSub);
            }
        }

        return output;
    }

    public static Matrix inverseQuantize(Matrix input, int blockSize, double quality, boolean matrixY) {
        Matrix qMatrix = getQuantizationMatrix(blockSize, quality, matrixY);
        Matrix output = new Matrix(input.getRowDimension(), input.getColumnDimension());

        for (int row = 0; row < input.getRowDimension(); row += blockSize) {
            for (int col = 0; col < input.getColumnDimension(); col += blockSize) {

                Matrix sub = input.getMatrix(row, row + blockSize - 1, col, col + blockSize - 1);
                Matrix invQuantizedSub = sub.arrayTimes(qMatrix);
                output.setMatrix(row, row + blockSize - 1, col, col + blockSize - 1, invQuantizedSub);
            }
        }
        return output;
    }

    private static double customRound(double value) {
        if (value >= -0.2 && value <= 0.2) {
            return Math.round(value * 100.0) / 100.0;
        } else {
            return Math.round(value * 10.0) / 10.0;
        }
    }

    public static Matrix getQuantizationMatrix(int blockSize, double quality, boolean matrixY) {
        double[][] baseTable = matrixY ? Quantization.quantizationMatrix8Y : Quantization.quantizationMatrix8C;
        double[][] resizedTable = new double[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int r = (i * 8) / blockSize;
                int c = (j * 8) / blockSize;
                resizedTable[i][j] = baseTable[r][c];
            }
        }

        double alpha = 0;
        if (quality >= 1 && quality <= 50) {
            alpha = 50.0 / quality;
        } else if (quality > 50 && quality < 100) {
            alpha = 2.0 - (2.0 * quality) / 100.0;
        }

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                if (quality == 100) {
                    resizedTable[i][j] = 1.0;
                } else {
                    resizedTable[i][j] = resizedTable[i][j] * alpha;
                }
            }
        }

        return new Matrix(resizedTable);
    }
}
