package jpeg;

import Jama.Matrix;
import enums.SamplingType;
import enums.TransformType;

import javax.imageio.metadata.IIOMetadataFormatImpl;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Třída, která bude obsahovat všechnu práci s obrázkem.
 */

public class Process {
    private BufferedImage originalImage;
    private int imageHeight;
    private int imageWidth;

    private int[][] originalRed, modifiedRed;
    private int[][] originalGreen, modifiedGreen;
    private int[][] originalBlue, modifiedBlue;

    private Matrix originalY, modifiedY;
    private Matrix originalCb, modifiedCb;
    private Matrix originalCr, modifiedCr;


    public Process(BufferedImage image) {

        this.originalImage = image;
        this.imageHeight = image.getHeight();
        this.imageWidth = image.getWidth();

        originalRed = new int[imageHeight][imageWidth];
        originalGreen = new int[imageHeight][imageWidth];
        originalBlue = new int[imageHeight][imageWidth];

        modifiedRed = new int[imageHeight][imageWidth];
        modifiedGreen = new int[imageHeight][imageWidth];
        modifiedBlue = new int[imageHeight][imageWidth];

        setOriginalRGB();
        copyOriginalToModified();
    }

    public void downSample (SamplingType samplingType) {
        modifiedY = Sampling.sampleDown(modifiedY, samplingType);
        modifiedCb = Sampling.sampleDown(modifiedCb, samplingType);
        modifiedCr = Sampling.sampleDown(modifiedCr, samplingType);
        imageWidth= modifiedY.getColumnDimension();
        imageHeight = modifiedY.getRowDimension();
    }

    public void upSample (SamplingType samplingType) {
        modifiedY = Sampling.sampleUp(modifiedY, samplingType);
        modifiedCb = Sampling.sampleUp(modifiedCb, samplingType);
        modifiedCr = Sampling.sampleUp(modifiedCr, samplingType);
        imageWidth= modifiedY.getColumnDimension();
        imageHeight = modifiedY.getRowDimension();
    }

    private void setOriginalRGB ()
    {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                Color color = new Color(originalImage.getRGB(x, y));

                originalRed[y][x] = color.getRed();
                originalGreen[y][x] = color.getGreen();
                originalBlue[y][x] = color.getBlue();
            }
        }
    }

    private void copyOriginalToModified() {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                modifiedRed[y][x] = originalRed[y][x];
                modifiedGreen[y][x] = originalGreen[y][x];
                modifiedBlue[y][x] = originalBlue[y][x];
            }
        }
    }

    public BufferedImage getImageFromRGB(int[][] red, int[][] green, int[][] blue) {

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                int r = red[y][x];
                int g = green[y][x];
                int b = blue[y][x];

                Color color = new Color(r, g, b);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }

    public void convertToYCbCr() {

        Matrix[] result = ColorTransform.convertOriginalRGBtoYcBcR(
                modifiedRed,
                modifiedGreen,
                modifiedBlue
        );

        modifiedY = result[0];
        modifiedCb = result[1];
        modifiedCr = result[2];

        System.out.println("converted to YCbCr");
    }

    public void convertToRGB() {

        int[][][] result = ColorTransform.convertModifiedYcBcRtoRGB(
                modifiedY,
                modifiedCb,
                modifiedCr
        );

        modifiedRed = result[0];
        modifiedGreen = result[1];
        modifiedBlue = result[2];

        System.out.println("converted to RGB");
    }

    public BufferedImage getImageFromYCbCr(Matrix matrix) {

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                int value = (int) Math.round(matrix.get(y, x));

                value = Math.max(0, Math.min(255, value));

                Color color = new Color(value, value, value);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }

    public BufferedImage getOneColorImage(int[][] colorMatrix) {

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                int value = colorMatrix[y][x];

                value = Math.max(0, Math.min(255, value));

                Color color = new Color(value, value, value);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }

    public double[] calculateQualityMetrics() {
        double[][] oR = Quality.convertIntToDouble(originalRed);
        double[][] mR = Quality.convertIntToDouble(modifiedRed);
        double[][] oG = Quality.convertIntToDouble(originalGreen);
        double[][] mG = Quality.convertIntToDouble(modifiedGreen);
        double[][] oB = Quality.convertIntToDouble(originalBlue);
        double[][] mB = Quality.convertIntToDouble(modifiedBlue);

        double mseR = Quality.countMSE(oR, mR);
        double mseG = Quality.countMSE(oG, mG);
        double mseB = Quality.countMSE(oB, mB);

        double mse = (mseR + mseG + mseB) / 3.0;
        double mae = (Quality.countMAE(oR, mR) + Quality.countMAE(oG, mG) + Quality.countMAE(oB, mB)) / 3.0;
        double sae = (Quality.countSAE(oR, mR) + Quality.countSAE(oG, mG) + Quality.countSAE(oB, mB)) / 3.0;
        double psnr = Quality.countPSNRforRGB(mseR, mseG, mseB);

        return new double[]{mse, mae, sae, psnr};
    }

    public void performQuantize(int blockSize, double quality) {
        this.modifiedY = Quantization.quantize(this.modifiedY, blockSize, quality, true);
        this.modifiedCb = Quantization.quantize(this.modifiedCb, blockSize, quality, false);
        this.modifiedCr = Quantization.quantize(this.modifiedCr, blockSize, quality, false);
    }

    public void performInverseQuantize(int blockSize, double quality) {
        this.modifiedY = Quantization.inverseQuantize(this.modifiedY, blockSize, quality, true);
        this.modifiedCb = Quantization.inverseQuantize(this.modifiedCb, blockSize, quality, false);
        this.modifiedCr = Quantization.inverseQuantize(this.modifiedCr, blockSize, quality, false);
    }

    public void performTransform(TransformType type, int blockSize) {
        this.modifiedY = Transform.transform(this.modifiedY, type, blockSize);
        this.modifiedCb = Transform.transform(this.modifiedCb, type, blockSize);
        this.modifiedCr = Transform.transform(this.modifiedCr, type, blockSize);
    }

    public void performInverseTransform(TransformType type, int blockSize) {
        this.modifiedY = Transform.inverseTransform(this.modifiedY, type, blockSize);
        this.modifiedCb = Transform.inverseTransform(this.modifiedCb, type, blockSize);
        this.modifiedCr = Transform.inverseTransform(this.modifiedCr, type, blockSize);
    }

    public Matrix getModifiedY() {
        return modifiedY;
    }

    public Matrix getModifiedCb() {
        return modifiedCb;
    }

    public Matrix getModifiedCr() {
        return modifiedCr;
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public int[][] getModifiedRed() {
        return modifiedRed;
    }

    public int[][] getModifiedGreen() {
        return modifiedGreen;
    }

    public int[][] getModifiedBlue() {
        return modifiedBlue;
    }

    public int[][] getOriginalRed() {
        return originalRed;
    }

    public int[][] getOriginalGreen() {
        return originalGreen;
    }

    public int[][] getOriginalBlue() {
        return originalBlue;
    }



}
