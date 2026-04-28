package components;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.IJ;
import java.io.File;

public class Attacks {


    public static ImagePlus jpegCompression(ImagePlus img, int quality) {
        String path = "temp_attack.jpg";
        IJ.saveAs(img, "Jpeg", path); 
        ImagePlus attacked = IJ.openImage(path);
        new File(path).delete();
        return attacked;
    }

    public static ImagePlus pngCompression(ImagePlus img) {
        String path = "temp_attack.png";
        IJ.saveAs(img, "Png", path);
        ImagePlus attacked = IJ.openImage(path);
        new File(path).delete();
        return attacked;
    }


    public static ImagePlus rotate(ImagePlus img, double angle) {
        ImagePlus copy = img.duplicate();
        ImageProcessor ip = copy.getProcessor();
        ip.setInterpolationMethod(ImageProcessor.BILINEAR);
        ip.rotate(angle);
        return copy;
    }

    public static ImagePlus resize(ImagePlus img, double scale) {
        int newW = (int) (img.getWidth() * scale);
        int newH = (int) (img.getHeight() * scale);
        ImageProcessor ip = img.getProcessor().resize(newW, newH, true);
        return new ImagePlus("Resized", ip);
    }

    public static ImagePlus mirror(ImagePlus img) {
        ImagePlus copy = img.duplicate();
        copy.getProcessor().flipHorizontal();
        return copy;
    }

    public static ImagePlus crop(ImagePlus img, int x, int y, int w, int h) {
        ImagePlus copy = img.duplicate();
        copy.getProcessor().setRoi(x, y, w, h);
        return new ImagePlus("Cropped", copy.getProcessor().crop());
    }

    public static ImagePlus restoreSize(ImagePlus attacked, int originalW, int originalH) {
        ImageProcessor ip = attacked.getProcessor().resize(originalW, originalH, true);
        return new ImagePlus("RestoredSize", ip);
    }

    public static ImagePlus padBack(ImagePlus cropped, int origW, int origH, int x, int y) {
        ImageProcessor ip = cropped.getProcessor().createProcessor(origW, origH);
        ip.insert(cropped.getProcessor(), x, y);
        return new ImagePlus("Padded", ip);
    }
}
