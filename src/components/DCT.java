package components;

import ij.process.ColorProcessor;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

public class DCT {

    private int u1 = 3, v1 = 1;
    private int u2 = 4, v2 = 1;

    public void embed(ColorProcessor host, ByteProcessor watermark, int h, int key) {
        int w = host.getWidth();
        int h_img = host.getHeight();
        byte[] wPixels = (byte[]) watermark.getPixels();
        byte[] permutedW = Permutator.permute(wPixels, key);

        for (int row = 0; row < h_img; row += 8) {
            for (int col = 0; col < w; col += 8) {
                float[][] block = getBlockY(host, col, row);
                
                float[][] dctBlock = applyDCT(block);

                int bitIndex = ((row / 8) * (w / 8) + (col / 8)) % permutedW.length;
                int bitW = ((permutedW[bitIndex] & 0xFF) > 128) ? 1 : 0;

                modifyCoefficients(dctBlock, bitW, h);

                float[][] idctBlock = applyIDCT(dctBlock);
                setBlockY(host, col, row, idctBlock);
            }
        }
    }

    private void modifyCoefficients(float[][] block, int bit, int h) {
        float b1 = block[u1][v1];
        float b2 = block[u2][v2];

        if (bit == 0) {
            if (!(b1 > b2)) {
                float tmp = b1; b1 = b2; b2 = tmp;
            }
        } else {
            if (!(b1 <= b2)) {
                float tmp = b1; b1 = b2; b2 = tmp;
            }
        }

        if (Math.abs(b1 - b2) < h) {
            if (b1 > b2) { b1 += h/2.0; b2 -= h/2.0; }
            else { b1 -= h/2.0; b2 += h/2.0; }
        }

        block[u1][v1] = b1;
        block[u2][v2] = b2;
    }

    public float[][] applyDCT(float[][] f) {
        float[][] F = new float[8][8];
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                float sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += f[x][y] * Math.cos((2 * x + 1) * u * Math.PI / 16.0) 
                                       * Math.cos((2 * y + 1) * v * Math.PI / 16.0);
                    }
                }
                float cu = (u == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                float cv = (v == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                F[u][v] = 0.25f * cu * cv * sum;
            }
        }
        return F;
    }

    public float[][] applyIDCT(float[][] F) {
        float[][] f = new float[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                float sum = 0;
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        float cu = (u == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                        float cv = (v == 0) ? (float)(1/Math.sqrt(2)) : 1.0f;
                        sum += cu * cv * F[u][v] * Math.cos((2 * x + 1) * u * Math.PI / 16.0) 
                                                 * Math.cos((2 * y + 1) * v * Math.PI / 16.0);
                    }
                }
                f[x][y] = 0.25f * sum;
            }
        }
        return f;
    }

    public ByteProcessor extract(ColorProcessor watermarked, int key, int wW, int wH) {
        byte[] extracted = new byte[wW * wH];
        int w = watermarked.getWidth();
        
        for (int i = 0; i < extracted.length; i++) {
            int blockCol = (i % (w / 8)) * 8;
            int blockRow = (i / (w / 8)) * 8;
            
            float[][] block = getBlockY(watermarked, blockCol, blockRow);
            float[][] dctBlock = applyDCT(block);
            
            if (dctBlock[u1][v1] > dctBlock[u2][v2]) extracted[i] = 0;
            else extracted[i] = (byte)255;
        }
        
        ByteProcessor res = new ByteProcessor(wW, wH);
        res.setPixels(Permutator.unpermute(extracted, key));
        return res;
    }

    private float[][] getBlockY(ColorProcessor cp, int x, int y) {
        float[][] block = new float[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int[] rgb = cp.getPixel(x + i, y + j, null);
                block[i][j] = (float)(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
            }
        }
        return block;
    }

    private void setBlockY(ColorProcessor cp, int x, int y, float[][] block) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int val = Math.min(255, Math.max(0, Math.round(block[i][j])));
                cp.putPixel(x + i, y + j, new int[]{val, val, val});
            }
        }
    }
}
