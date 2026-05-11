package components;

import ij.process.ColorProcessor;
import ij.process.ByteProcessor;

public class DCT {

    private int u1 = 3, v1 = 1;
    private int u2 = 4, v2 = 1;

    public void embed(ColorProcessor host, ByteProcessor watermark, int h, int key, int blockSize) {
        int w = host.getWidth();
        int h_img = host.getHeight();
        byte[] wPixels = (byte[]) watermark.getPixels();
        byte[] permutedW = Permutator.permute(wPixels, key);

        for (int row = 0; row <= h_img - blockSize; row += blockSize) {
            for (int col = 0; col <= w - blockSize; col += blockSize) {
                float[][] block = getBlockY(host, col, row, blockSize);
                float[][] dctBlock = applyDCT(block, blockSize);

                int bitIndex = ((row / blockSize) * (w / blockSize) + (col / blockSize)) % permutedW.length;
                int bitW = ((permutedW[bitIndex] & 0xFF) > 128) ? 1 : 0;

                modifyCoefficients(dctBlock, bitW, h);

                float[][] idctBlock = applyIDCT(dctBlock, blockSize);
                setBlockY(host, col, row, idctBlock, blockSize);
            }
        }
    }

    public byte[] extract(ColorProcessor watermarked, int key, int wW, int wH, int blockSize) {
        byte[] extracted = new byte[wW * wH];
        int w = watermarked.getWidth();

        for (int i = 0; i < extracted.length; i++) {
            int blockCol = (i % (w / blockSize)) * blockSize;
            int blockRow = (i / (w / blockSize)) * blockSize;

            if (blockRow + blockSize <= watermarked.getHeight() && blockCol + blockSize <= w) {
                float[][] block = getBlockY(watermarked, blockCol, blockRow, blockSize);
                float[][] dctBlock = applyDCT(block, blockSize);

                if (dctBlock[u1][v1] > dctBlock[u2][v2]) extracted[i] = 0;
                else extracted[i] = (byte)255;
            }
        }
        return extracted; 
    }

    public float[][] applyDCT(float[][] f, int N) {
        float[][] F = new float[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                float sum = 0;
                for (int x = 0; x < N; x++) {
                    for (int y = 0; y < N; y++) {
                        sum += f[x][y] * Math.cos((2 * x + 1) * u * Math.PI / (2.0 * N))
                                * Math.cos((2 * y + 1) * v * Math.PI / (2.0 * N));
                    }
                }
                float cu = (u == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                float cv = (v == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                F[u][v] = (2.0f / N) * cu * cv * sum;
            }
        }
        return F;
    }

    public float[][] applyIDCT(float[][] F, int N) {
        float[][] f = new float[N][N];
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                float sum = 0;
                for (int u = 0; u < N; u++) {
                    for (int v = 0; v < N; v++) {
                        float cu = (u == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                        float cv = (v == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                        sum += cu * cv * F[u][v] * Math.cos((2 * x + 1) * u * Math.PI / (2.0 * N))
                                * Math.cos((2 * y + 1) * v * Math.PI / (2.0 * N));
                    }
                }
                f[x][y] = (2.0f / N) * sum;
            }
        }
        return f;
    }

    private float[][] getBlockY(ColorProcessor cp, int x, int y, int N) {
        float[][] block = new float[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int[] rgb = cp.getPixel(x + i, y + j, null);
                block[i][j] = (float)(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
            }
        }
        return block;
    }

    private void setBlockY(ColorProcessor cp, int x, int y, float[][] block, int N) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int[] rgb = cp.getPixel(x + i, y + j, null);
                double Cb = 128 - 0.168736 * rgb[0] - 0.331264 * rgb[1] + 0.5 * rgb[2];
                double Cr = 128 + 0.5 * rgb[0] - 0.418688 * rgb[1] - 0.081312 * rgb[2];
                double newY = block[i][j];

                int r = (int)Math.round(newY + 1.402 * (Cr - 128));
                int g = (int)Math.round(newY - 0.344136 * (Cb - 128) - 0.714136 * (Cr - 128));
                int b = (int)Math.round(newY + 1.772 * (Cb - 128));

                cp.putPixel(x + i, y + j, new int[]{
                        Math.min(255, Math.max(0, r)),
                        Math.min(255, Math.max(0, g)),
                        Math.min(255, Math.max(0, b))
                });
            }
        }
    }

    private void modifyCoefficients(float[][] block, int bit, int h) {
        float b1 = block[u1][v1];
        float b2 = block[u2][v2];
        if (bit == 0) {
            if (!(b1 > b2)) { float tmp = b1; b1 = b2; b2 = tmp; }
        } else {
            if (!(b1 <= b2)) { float tmp = b1; b1 = b2; b2 = tmp; }
        }

        if (Math.abs(b1 - b2) < h) {
            if (b1 > b2) { b1 += h/2.0; b2 -= h/2.0; }
            else { b1 -= h/2.0; b2 += h/2.0; }
        }
        block[u1][v1] = b1;
        block[u2][v2] = b2;
    }
}
