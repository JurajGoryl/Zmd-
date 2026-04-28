package jpeg;

import Jama.Matrix;
import enums.TransformType;

public class Transform {

    public static Matrix getTransformMatrix(TransformType type, int blockSize) {
        if (type == TransformType.DCT) {
            return getDCTMatrix(blockSize);
        } else if (type == TransformType.WHT) {
            return getWHTMatrix(blockSize);
        }
        throw new IllegalArgumentException("Unknown transform type: " + type);
    }

    public static Matrix transform(Matrix input, TransformType type, int blockSize) {
        Matrix A = getTransformMatrix(type, blockSize);
        Matrix AT = A.transpose();
        int rows = input.getRowDimension();
        int cols = input.getColumnDimension();
        Matrix output = new Matrix(rows, cols);

        for (int r = 0; r < rows; r += blockSize) {
            for (int c = 0; c < cols; c += blockSize) {
                Matrix sub = input.getMatrix(r, r + blockSize - 1, c, c + blockSize - 1);
                Matrix transformed = A.times(sub).times(AT);
                output.setMatrix(r, r + blockSize - 1, c, c + blockSize - 1, transformed);
            }
        }
        return output;
    }

    public static Matrix inverseTransform(Matrix input, TransformType type, int blockSize) {
        Matrix A = getTransformMatrix(type, blockSize);
        Matrix AT = A.transpose();
        int rows = input.getRowDimension();
        int cols = input.getColumnDimension();
        Matrix output = new Matrix(rows, cols);

        for (int r = 0; r < rows; r += blockSize) {
            for (int c = 0; c < cols; c += blockSize) {
                Matrix sub = input.getMatrix(r, r + blockSize - 1, c, c + blockSize - 1);
                Matrix inverted = AT.times(sub).times(A);
                output.setMatrix(r, r + blockSize - 1, c, c + blockSize - 1, inverted);
            }
        }
        return output;
    }

    private static Matrix getDCTMatrix(int N) {
        Matrix A = new Matrix(N, N);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == 0) {
                    A.set(i, j, Math.sqrt(1.0 / N) * Math.cos(((2.0 * j + 1.0) * i * Math.PI) / (2.0 * N)));
                } else {
                    A.set(i, j, Math.sqrt(2.0 / N) * Math.cos(((2.0 * j + 1.0) * i * Math.PI) / (2.0 * N)));
                }
            }
        }
        A.print(2, 2);
        return A;
    }

    private static Matrix getWHTMatrix(int N) {
        Matrix H = new Matrix(1, 1);
        H.set(0, 0, 1);
        int currentSize = 1;

        while (currentSize < N) {
            Matrix nextH = new Matrix(currentSize * 2, currentSize * 2);
            for (int r = 0; r < currentSize; r++) {
                for (int c = 0; c < currentSize; c++) {
                    double val = H.get(r, c);
                    nextH.set(r, c, val);                                      // Horní levá
                    nextH.set(r, c + currentSize, val);                    // Horní pravá
                    nextH.set(r + currentSize, c, val);                     // Dolní levá
                    nextH.set(r + currentSize, c + currentSize, -val);  // Dolní pravá
                }
            }
            H = nextH;
            currentSize *= 2;
        }

        H = H.times(1.0 / Math.sqrt(N));
        H.print(2, 2);
        return H;
    }
}