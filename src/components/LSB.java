package components;

import ij.process.ColorProcessor;
import ij.process.ByteProcessor;

public class LSB {

    public void embed(ColorProcessor imageUsed, ByteProcessor watermark, int h, int key, String channel) {
        int w = imageUsed.getWidth();
        int h_img = imageUsed.getHeight();
        byte[] wPixels = (byte[]) watermark.getPixels();
        byte[] permutedW = Permutator.permute(wPixels, key);

        for (int i = 0; i < w * h_img; i++) {
            int x = i % w;
            int y = i / w;
            int[] rgb = imageUsed.getPixel(x, y, null);

            double Y  =  0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2];
            double Cb = 128 - 0.168736 * rgb[0] - 0.331264 * rgb[1] + 0.5 * rgb[2];
            double Cr = 128 + 0.5 * rgb[0] - 0.418688 * rgb[1] - 0.081312 * rgb[2];

            int bitW = ((permutedW[i % permutedW.length] & 0xFF) > 128) ? 1 : 0;

            if (channel.equalsIgnoreCase("Cb")) {
                Cb = (Math.round(Cb) & ~(1 << h)) | (bitW << h);
            } else if (channel.equalsIgnoreCase("Cr")) {
                Cr = (Math.round(Cr) & ~(1 << h)) | (bitW << h);
            } else {
                Y = (Math.round(Y) & ~(1 << h)) | (bitW << h);
            }

            int r = (int)Math.round(Y + 1.402 * (Cr - 128));
            int g = (int)Math.round(Y - 0.344136 * (Cb - 128) - 0.714136 * (Cr - 128));
            int b = (int)Math.round(Y + 1.772 * (Cb - 128));

            imageUsed.putPixel(x, y, new int[]{
                    Math.min(255, Math.max(0, r)),
                    Math.min(255, Math.max(0, g)),
                    Math.min(255, Math.max(0, b))
            });
        }
    }

    public byte[] extract(ColorProcessor watermarked, int h, int key, int wW, int wH, String channel) {
        byte[] extracted = new byte[wW * wH];
        int w = watermarked.getWidth();

        for (int i = 0; i < extracted.length; i++) {
            int x = i % w;
            int y = i / w;
            int[] rgb = watermarked.getPixel(x, y, null);

            double value;
            if (channel.equalsIgnoreCase("Cb")) {
                value = 128 - 0.168736 * rgb[0] - 0.331264 * rgb[1] + 0.5 * rgb[2];
            } else if (channel.equalsIgnoreCase("Cr")) {
                value = 128 + 0.5 * rgb[0] - 0.418688 * rgb[1] - 0.081312 * rgb[2];
            } else {
                value = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2];
            }

            int bit = ((int)Math.round(value) >> h) & 1;
            extracted[i] = (byte) (bit == 1 ? 255 : 0);
        }
        return extracted;
    }
}
