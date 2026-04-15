package components;

import ij.process.ColorProcessor;
import ij.process.ByteProcessor;

public class Watermark {

    public void embed(ColorProcessor imageUsed, ByteProcessor watermark, int h, int key, String channel) {
        int w = imageUsed.getWidth();
        int h_img = imageUsed.getHeight();
        byte[] wPixels = (byte[]) watermark.getPixels(); // we make a byte array of the watermark pixels
        byte[] permutedW = Permutator.permute(wPixels, key); // permute the watermark pixels using the provided key || uncomment to see watermark unpermutated

        for (int i = 0; i < w * h_img; i++) {
            // calculate x y coordinates
            int x = i % w;
            int y = i / w;
            int[] rgb = imageUsed.getPixel(x, y, null); // 0 - R, 1 - G, 2 -B

            // Calculate the value of the selected channel
            double value;
            if (channel.equalsIgnoreCase("Cb")){
                value = 128 - 0.168 * rgb[0] - 0.331 * rgb[1] + 0.5 * rgb[2];
            }
            else if (channel.equalsIgnoreCase("Cr")){
                value = 128 + 0.5 * rgb[0] - 0.418 * rgb[1] - 0.081 * rgb[2];
            } else {
                value = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2];
            } // Default Y

            // we use modulo to repeat the watermark if the image is larger than the watermark
            int pixelValue = (int) value;
            int bitW = ((permutedW[i %  permutedW.length] & 0xFF) > 128) ? 1 : 0; // if its greater than 128 it is white || replace permutedW with wPixels to see unpermutated watermark

            // first we create 1 on a h position then we tilda it and filter it with pixelValue and then we add the bitW on the h position
            pixelValue = (pixelValue & ~(1 << h)) | (bitW << h);

            // we set the new pixel value to all channels to keep the color of the image
            imageUsed.putPixel(x, y, new int[]{pixelValue, pixelValue, pixelValue});
        }
    }

    public ByteProcessor extract(ColorProcessor watermarked, int h, int key, int wW, int wH) {

        byte[] extracted = new byte[wW * wH]; // we create a byte array to store the extracted watermark pixels

        for (int i = 0; i < extracted.length; i++) {
            int[] rgb = watermarked.getPixel(i % watermarked.getWidth(), i / watermarked.getWidth(), null); // we calculate x with modulo bcs it tells us how many pixels are left in the current row and y with division bcs it tells us how many rows we have passed
            int bit = (rgb[0] >> h) & 1; // we extract the bit moved by h by shifting it to the end and filtering it with 1
            extracted[i] = (byte) (bit == 1 ? 255 : 0); // if its white or black
        }

        ByteProcessor result = new ByteProcessor(wW, wH);

        // we unpermute it with the same key
        result.setPixels(Permutator.unpermute(extracted, key));
        return result;
    }
}
