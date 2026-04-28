package core;

import java.io.*;
import java.util.ArrayList;

public class Excel {
    public static void saveToCSV(ArrayList<String[]> data) {
        try (PrintWriter pw = new PrintWriter(new File("vysledky_odolnosti.csv"), "UTF-8")) {

            pw.write('\ufeff');

            pw.println("sep=;");

            for (String[] row : data) {
                pw.println(String.join(";", row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
