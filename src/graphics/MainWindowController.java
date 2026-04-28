package graphics;

import components.Attacks;
import components.DCT;
import components.LSB;
import core.FileBindings;
import core.Helper;
import enums.SamplingType;
import enums.TransformType;
import ij.ImagePlus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jpeg.Process;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import graphics.Dialogs;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import java.util.ArrayList;


public class MainWindowController implements Initializable {
    @FXML
    Button buttonInverseQuantize;
    @FXML
    Button buttonInverseToRGB;
    @FXML
    Button buttonInverseSample;
    @FXML
    Button buttonInverseTransform;
    @FXML
    Button buttonQuantize;
    @FXML
    Button buttonSample;
    @FXML
    Button buttonToYCbCr;
    @FXML
    Button buttonTransform;

    @FXML
    TextField qualityPSNR;
    @FXML
    TextField qualityMSE;
    @FXML
    TextField qualityMAE;
    @FXML
    TextField qualitySAE;

    @FXML
    Slider quantizeQuality;
    @FXML
    TextField quantizeQualityField;

    @FXML
    CheckBox shadesOfGrey;
    @FXML
    CheckBox showSteps;

    @FXML
    Spinner<Integer> transformBlock;
    @FXML
    ComboBox<TransformType> transformType;
    @FXML
    ComboBox<SamplingType> sampling;





    /**
     * Inicializace okna, nastavení výchozích hodnot. Naplnění prvků v rozhraní.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nastavení všech hodnot do combo boxů
        sampling.getItems().setAll(SamplingType.values());
        transformType.getItems().setAll(TransformType.values());

        // Nastavení výchozích hodnot
        sampling.getSelectionModel().select(SamplingType.S_4_4_4);
        transformType.getSelectionModel().select(TransformType.DCT);
        quantizeQuality.setValue(50);

        // Vytvoření listu možností, které budou uvnitř spinneru
        ObservableList<Integer> blocks = FXCollections.observableArrayList(2, 4, 8, 16, 32, 64, 128, 256, 512);
        SpinnerValueFactory<Integer> spinnerValues = new SpinnerValueFactory.ListSpinnerValueFactory<>(blocks);
        spinnerValues.setValue(8);
        transformBlock.setValueFactory(spinnerValues);


        // Nastavení formátu čísel v textových polích, aby bylo možné zadávat pouze čísla. Plus metoda, která je na konci souboru.
        quantizeQualityField.setTextFormatter(new TextFormatter<>(Helper.NUMBER_FORMATTER));

        // Propojení slideru s textovým polem
        quantizeQualityField.textProperty().bindBidirectional(quantizeQuality.valueProperty(), NumberFormat.getIntegerInstance());

        BufferedImage image = Dialogs.loadImageFromPath(FileBindings.defaultImage);
        process = new Process(image);
    }

    public void close() {
        Stage stage = ((Stage) buttonSample.getScene().getWindow());
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void closeWindows() {
        Dialogs.closeAllWindows();
    }

    private Process process;

    public void showOriginal() {
        File f = new File(FileBindings.defaultImage);

        try {
            Dialogs.showImageInWindow(ImageIO.read(f), "Original", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeImage() {
        File file = Dialogs.openFile();

        if (file != null) {

            BufferedImage image = Dialogs.loadImageFromPath(file);

            // zavře stará otevřená okna
            Dialogs.closeAllWindows();

            // vytvoří nový Process
            process = new Process(image);

        }
    }

    public void reset() {
    }

    public void showRGBModified() {
        BufferedImage img = process.getImageFromRGB(
                process.getModifiedRed(),
                process.getModifiedGreen(),
                process.getModifiedBlue()
        );

        Dialogs.showImageInWindow(img, "RGB reconstructed");
    }

    public void convertToRGB() {
        process.convertToRGB();
    }

    public void convertToYCbCr() {
        process.convertToYCbCr();
    }

    public void sample() {
        process.downSample(sampling.getValue());
    }

    public void inverseSample() {
        process.upSample(sampling.getValue());
    }

    public void transform() {
        TransformType selectedType = transformType.getValue();
        int blockSize = transformBlock.getValue();
        process.performTransform(selectedType, blockSize);
    }

    public void inverseTransform() {
        TransformType selectedType = transformType.getValue();
        int blockSize = transformBlock.getValue();
        process.performInverseTransform(selectedType, blockSize);
    }

    public void quantize() {
        double quantity = quantizeQuality.getValue();
        int blockSize = transformBlock.getValue();
        process.performQuantize(blockSize, quantity);
    }

    public void inverseQuantize() {
        double quantity = quantizeQuality.getValue();
        int blockSize = transformBlock.getValue();
        process.performInverseQuantize(blockSize, quantity);

    }

    @FXML
    public void countQuality() {
        double[] metrics = process.calculateQualityMetrics();
        qualityMSE.setText(String.format("%.2f", metrics[0]));
        qualityMAE.setText(String.format("%.2f", metrics[1]));
        qualitySAE.setText(String.format("%.2f", metrics[2]));
        qualityPSNR.setText(String.format("%.2f", metrics[3]));
    }

    public void showBlueModified() {
        BufferedImage img = process.getOneColorImage(
                process.getModifiedBlue()
        );

        Dialogs.showImageInWindow(img, "Modified Blue");
    }

    public void showBlueOriginal() {
        BufferedImage img = process.getOneColorImage(
                process.getOriginalBlue()
        );

        Dialogs.showImageInWindow(img, "Original Blue");
    }

    public void showCbModified() {
        BufferedImage img = process.getImageFromYCbCr(
                process.getModifiedCb()
        );

        Dialogs.showImageInWindow(img, "Cb component");
    }

    private String selectedLsbChannel = "Y";

    public void showCbOriginal() {
        selectedLsbChannel = "Cb";
    }

    public void showCrModified() {
        BufferedImage img = process.getImageFromYCbCr(
                process.getModifiedCr()
        );

        Dialogs.showImageInWindow(img, "Cr component");
    }

    public void showCrOriginal() {
        selectedLsbChannel = "Cr";
    }

    public void showGreenModified() {
        BufferedImage img = process.getOneColorImage(
                process.getModifiedGreen()
        );

        Dialogs.showImageInWindow(img, "Modified Green");
    }

    public void showGreenOriginal() {
            BufferedImage img = process.getOneColorImage(
                    process.getOriginalGreen()
            );

            Dialogs.showImageInWindow(img, "Original Green");
    }

    public void showRedModified() {
        BufferedImage img = process.getOneColorImage(
                process.getModifiedRed()
        );

        Dialogs.showImageInWindow(img, "Modified Red");
    }

    public void showRedOriginal() {
        BufferedImage img = process.getOneColorImage(
                process.getOriginalRed()
        );

        Dialogs.showImageInWindow(img, "Original Red");
    }

    public void showYModified() {
        BufferedImage img = process.getImageFromYCbCr(
                process.getModifiedY()
        );

        Dialogs.showImageInWindow(img, "Y component");
    }

    public void showYOriginal() {
        selectedLsbChannel = "Y";
    }

    private BufferedImage watermarkImage;
    private LSB lsbEngine = new LSB();
    private DCT dctEngine = new DCT();

    @FXML
    public void loadWatermark() {
        File file = Dialogs.openFile();
        if (file != null) {
            watermarkImage = Dialogs.loadImageFromPath(file);
        }
}

    @FXML
    public void runFinalTest() {
        if (process == null || watermarkImage == null) return;

        File resultsDir = new File("results");
        if (!resultsDir.exists()) resultsDir.mkdir();

        ArrayList<String[]> logs = new ArrayList<>();
        logs.add(new String[]{"Method", "Attack", "h", "Status", "Similarity", "Folder Path"});

        int key = 1020202;
        int h_lsb = 7;
        int h_dct = 50;
        int w = watermarkImage.getWidth();
        int h = watermarkImage.getHeight();
        int oW = process.getOriginalImage().getWidth();
        int oH = process.getOriginalImage().getHeight();

        ByteProcessor waterBP = (ByteProcessor) new ImagePlus("W", watermarkImage).getProcessor().convertToByte(false);
        String[] methods = {"LSB", "DCT"};

        for (String m : methods) {
            int h_val = m.equals("LSB") ? h_lsb : h_dct;
            ImagePlus host = new ImagePlus(m, process.getOriginalImage());

            if (m.equals("LSB")) {
                lsbEngine.embed((ColorProcessor)host.getProcessor(), waterBP, h_val, key, selectedLsbChannel);
            } else {
                dctEngine.embed((ColorProcessor)host.getProcessor(), waterBP, h_val, key);
            }

            ImagePlus jpeg = Attacks.jpegCompression(host, 50);
            processAndLog(m, "JPEG_Q50", jpeg, jpeg, host, h_val, key, w, h, logs, waterBP);

            ImagePlus png = Attacks.pngCompression(host);
            processAndLog(m, "PNG", png, png, host, h_val, key, w, h, logs, waterBP);

            ImagePlus rot90raw = Attacks.rotate(host, 90);
            ImagePlus rot90sync = Attacks.rotate(rot90raw, -90);
            processAndLog(m, "Rotation_90", rot90raw, rot90sync, host, h_val, key, w, h, logs, waterBP);

            ImagePlus rot45raw = Attacks.rotate(host, 45);
            ImagePlus rot45sync = Attacks.rotate(rot45raw, -45);
            processAndLog(m, "Rotation_45", rot45raw, rot45sync, host, h_val, key, w, h, logs, waterBP);

            ImagePlus res75raw = Attacks.resize(host, 0.75);
            ImagePlus res75sync = Attacks.restoreSize(res75raw, oW, oH);
            processAndLog(m, "Resize_75", res75raw, res75sync, host, h_val, key, w, h, logs, waterBP);

            ImagePlus res50raw = Attacks.resize(host, 0.5);
            ImagePlus res50sync = Attacks.restoreSize(res50raw, oW, oH);
            processAndLog(m, "Resize_50", res50raw, res50sync, host, h_val, key, w, h, logs, waterBP);

            ImagePlus mirRaw = Attacks.mirror(host);
            ImagePlus mirSync = Attacks.mirror(mirRaw);
            processAndLog(m, "Mirroring", mirRaw, mirSync, host, h_val, key, w, h, logs, waterBP);

            ImagePlus cropRaw = Attacks.crop(host, 0, 0, oW/2, oH/2);
            ImagePlus cropSync = Attacks.padBack(cropRaw, oW, oH, 0, 0);
            processAndLog(m, "Cropping", cropRaw, cropSync, host, h_val, key, w, h, logs, waterBP);
        }

        core.Excel.saveToCSV(logs);
        Dialogs.showImageInWindow(null, "Test Suite 2026 Complete! Check 'results' folder.");
    }

    private void processAndLog(String method, String attack, ImagePlus rawAttacked, ImagePlus synchronizedImg, ImagePlus embeddedHost, int h_val, int key, int w, int h, ArrayList<String[]> logs, ByteProcessor originalWater) {
        String folderName = "results/" + method + "_" + attack;
        File dir = new File(folderName);
        if (!dir.exists()) dir.mkdirs();

        saveImg(process.getOriginalImage(), folderName + "/1_original_host.png");
        saveImg(embeddedHost.getBufferedImage(), folderName + "/2_embedded_host.png");
        saveImg(rawAttacked.getBufferedImage(), folderName + "/3_raw_attacked.png");
        saveImg(synchronizedImg.getBufferedImage(), folderName + "/4_attacked_synchronized.png");

        ij.process.ImageProcessor ip = synchronizedImg.getProcessor();
        if (!(ip instanceof ColorProcessor)) ip = ip.convertToColorProcessor();
        ColorProcessor cp = (ColorProcessor) ip;

        ByteProcessor extProc = method.equals("LSB")
                ? lsbEngine.extract(cp, h_val, key, w, h, selectedLsbChannel)
                : dctEngine.extract(cp, key, w, h);

        saveImg(new ImagePlus("Ext", extProc).getBufferedImage(), folderName + "/5_extracted_watermark.png");

        double similarity = calculateSimilarity(originalWater, extProc);
        String status = (similarity > 0.75) ? "PASS" : "FAIL";

        logs.add(new String[]{method, attack, String.valueOf(h_val), status, String.format("%.2f%%", similarity * 100), folderName});
    }

    private void performAttack(String method, String attack, ImagePlus attImg, int h_val, int key, int w, int h, int oW, int oH, String embPath, ArrayList<String[]> logs, ByteProcessor origWater) {
        String attPath = "results/" + method + "_attack_" + attack + ".png";
        saveImg(attImg.getBufferedImage(), attPath);

        ij.process.ImageProcessor ip = attImg.getProcessor();
        if (!(ip instanceof ColorProcessor)) ip = ip.convertToColorProcessor();
        ColorProcessor cp = (ColorProcessor) ip;

        ByteProcessor extProc = method.equals("LSB")
                ? lsbEngine.extract(cp, h_val, key, w, h, selectedLsbChannel)
                : dctEngine.extract(cp, key, w, h);


        double similarity = calculateSimilarity(origWater, extProc);
        String status = (similarity > 0.75) ? "PASS" : "FAIL";

        BufferedImage extImg = new ImagePlus("Extracted", extProc).getBufferedImage();
        String extPath = "results/" + method + "_ext_" + attack + ".png";
        saveImg(extImg, extPath);

        logs.add(new String[]{method, attack, String.valueOf(h_val), status, String.format("%.2f%%", similarity * 100), "0_host_orig.png", embPath, attPath, extPath});
    }

    private double calculateSimilarity(ByteProcessor orig, ByteProcessor ext) {
        int match = 0;
        byte[] o = (byte[]) orig.getPixels();
        byte[] e = (byte[]) ext.getPixels();
        for (int i = 0; i < o.length; i++) {
            if (((o[i] & 0xFF) > 128 && (e[i] & 0xFF) > 128) || ((o[i] & 0xFF) <= 128 && (e[i] & 0xFF) <= 128)) match++;
        }
        return (double) match / o.length;
    }

    private void saveImg(BufferedImage img, String name) {
        if (img == null) return;
        try { ImageIO.write(img, "png", new File(name)); } catch (IOException e) { e.printStackTrace(); }
    }
}

