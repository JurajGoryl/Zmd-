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

    public void showCbOriginal() {
    }

    public void showCrModified() {
        BufferedImage img = process.getImageFromYCbCr(
                process.getModifiedCr()
        );

        Dialogs.showImageInWindow(img, "Cr component");
    }

    public void showCrOriginal() {

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

        if (process == null || watermarkImage == null) {
            System.out.println("Chyba: Najprv musíš načítať hlavný obrázok aj vodoznak!");
            return;
        }

        int key = 1020202;
        int h_lsb = 7;
        int h_dct = 50;
        int wW = watermarkImage.getWidth();
        int wH = watermarkImage.getHeight();
        int origW = process.getOriginalImage().getWidth();
        int origH = process.getOriginalImage().getHeight();

        ImagePlus hostLSB = new ImagePlus("LSB Host", process.getOriginalImage());
        ImagePlus hostDCT = new ImagePlus("DCT Host", process.getOriginalImage());
        ByteProcessor waterBP = (ByteProcessor) new ImagePlus("W", watermarkImage).getProcessor().convertToByte(false);

        lsbEngine.embed((ColorProcessor)hostLSB.getProcessor(), waterBP, h_lsb, key, "Y");
        dctEngine.embed((ColorProcessor)hostDCT.getProcessor(), waterBP, h_dct, key);

        java.util.ArrayList<String[]> logs = new java.util.ArrayList<>();
        logs.add(new String[]{"Metoda", "Utok", "Parameter h", "Vysledok"});


        ImagePlus attJpeg = Attacks.jpegCompression(hostLSB, 50);
        logs.add(new String[]{"LSB", "JPEG (Q50)", String.valueOf(h_lsb), "Vykonané"});

        ImagePlus attJpegDCT = Attacks.jpegCompression(hostDCT, 50);
        logs.add(new String[]{"DCT", "JPEG (Q50)", String.valueOf(h_dct), "Vykonané"});

        ImagePlus attRot = Attacks.rotate(hostLSB, 90);
        ImagePlus preparedRot = Attacks.rotate(attRot, -90);
        logs.add(new String[]{"LSB", "Rotácia 90", String.valueOf(h_lsb), "Synchronizované"});

        ImagePlus attSize = Attacks.resize(hostDCT, 0.5);
        ImagePlus preparedSize = Attacks.restoreSize(attSize, origW, origH);
        logs.add(new String[]{"DCT", "Resize 50%", String.valueOf(h_dct), "Obnovené"});

        ImagePlus attMirror = Attacks.mirror(hostLSB);
        ImagePlus preparedMirror = Attacks.mirror(attMirror); // Spätný flip
        logs.add(new String[]{"LSB", "Zrkadlenie", String.valueOf(h_lsb), "OK"});

        ImagePlus attCrop = Attacks.crop(hostDCT, 0, 0, origW/2, origH/2);
        ImagePlus preparedCrop = Attacks.padBack(attCrop, origW, origH, 0, 0);
        logs.add(new String[]{"DCT", "Orezanie", String.valueOf(h_dct), "Padding hotový"});

        String[][] finalResults = logs.toArray(new String[0][0]);
        core.Excel.saveToCSV(finalResults);

        Dialogs.showImageInWindow(null, "Testy odolnosti prebehli úspešne! Súbor 'vysledky_odolnosti.csv' bol vytvorený.");
    }
}

