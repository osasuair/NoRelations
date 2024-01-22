import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ExportToCSV {

    public static boolean exportToCSV(Table table, String fileName) {
        try {
            Paths.get(fileName + ".csv");
            PrintWriter writer = new PrintWriter(fileName + ".csv", StandardCharsets.UTF_8);
            String tableString = table.toString()
                    .replace("{", "")
                    .replace("}", "")
                    .replace(", ", ",")
                    .strip();
            writer.println(tableString);
            writer.println();
            writer.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
}
