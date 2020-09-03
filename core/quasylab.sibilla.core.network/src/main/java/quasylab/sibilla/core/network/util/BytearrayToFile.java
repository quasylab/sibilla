package quasylab.sibilla.core.network.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class BytearrayToFile {

    public static void toFile(byte[] bytes, String dirName, String fileName) throws IOException {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtils.writeByteArrayToFile(file, bytes);
    }

    public static byte[] fromFile(String dirName, String fileName) throws IOException {
        File dir = new File(dirName);
        File file = new File(dir, fileName);
        return FileUtils.readFileToByteArray(file);
    }
}
