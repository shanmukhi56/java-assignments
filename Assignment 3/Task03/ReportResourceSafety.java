import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReportResourceSafety {
    public static void main(String[] args) {
        String[] inputFiles = {"store1.txt", "store2.txt", "store3.txt"};
        int total = 0;

        try (BufferedWriter out = new BufferedWriter(new FileWriter("summary.txt"))) {
            for (String file : inputFiles) {
                try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        total += Integer.parseInt(line.trim());
                    }
                    out.write(file + " processed");
                    out.newLine();
                } catch (IOException | NumberFormatException e) {
                    System.out.println("Failed file: " + file + " - " + e.getMessage());
                }
            }
            out.write("Total sales: " + total);
            out.newLine();
            System.out.println("Report created. Total sales: " + total);
        } catch (IOException e) {
            System.out.println("Could not write report: " + e.getMessage());
        }
    }
}
