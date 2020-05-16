package quasylab.sibilla.core.server.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compressor {

    public static byte[] compress(byte[] uncompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(uncompressedData.length);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(uncompressedData);
            gzipOutputStream.close();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] uncompress(byte[] compressedData) {
        byte[] result = new byte[]{};
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            result = gzipInputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
