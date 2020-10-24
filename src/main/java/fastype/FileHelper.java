package fastype;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileHelper {
    public static String readFile(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            return String.join("\n", lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
