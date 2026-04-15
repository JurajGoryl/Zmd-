import components.Watermark;

import ij.IJ;
import ij.ImagePlus;

public class Main {
    public static void main(String[] args) {

        // load images
        ImagePlus imageUsed = IJ.openImage("./img/tests/lena_std.jpg");
        ImagePlus watermark = IJ.openImage("./img/watermark.png");

        if (imageUsed == null || watermark == null) {
            System.out.println("Images not found");
            return;
        }

        Watermark temp = new Watermark();
        int h = 3; // depth 0 - 7
        int key = 1020202;

        // Insertion
        temp.embed((ij.process.ColorProcessor)imageUsed.getProcessor(),
                (ij.process.ByteProcessor)watermark.getProcessor().convertToByte(false),h, key, "Y"); // convertToByte to grayscale the watermark
        imageUsed.show();
        IJ.save(imageUsed, "watermarked.png");

        // Extraction
        ImagePlus ext = new ImagePlus("Extracted",
                temp.extract((ij.process.ColorProcessor)imageUsed.getProcessor(), h, key, watermark.getWidth(), watermark.getHeight()));
        ext.show();
    }
}
