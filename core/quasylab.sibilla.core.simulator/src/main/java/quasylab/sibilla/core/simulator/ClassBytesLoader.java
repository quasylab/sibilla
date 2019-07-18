package quasylab.sibilla.core.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClassBytesLoader {

    public static byte[] loadClassBytes(String className) throws IOException {
        int size;
        byte[] classBytes;
        InputStream is;
        String fileSeparator = System.getProperty("file.separator");

        className = className.replace('.', fileSeparator.charAt(0));
        className = className + ".class";

        // Search for the class in the CLASSPATH
        is = ClassLoader.getSystemResourceAsStream(className);

        if (is == null)
            throw new FileNotFoundException(className);

        size = is.available();

        classBytes = new byte[size];

        is.read(classBytes);
        is.close();

        return classBytes;
    }
}