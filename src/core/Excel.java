package core;
import java.io.PrintWriter;
import java.io.File;

public class Excel {
    public static void saveToCSV(String[][] results) {
        try (PrintWriter pw = new PrintWriter(new File("vysledky_odolnosti.csv"))) {
            for (String[] row : results) {
                pw.println(String.join(";", row));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
