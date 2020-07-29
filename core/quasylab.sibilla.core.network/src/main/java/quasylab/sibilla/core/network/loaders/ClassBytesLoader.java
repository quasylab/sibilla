/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.loaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class used to extract the data associated to the .class file of a compiled Java class.
 *
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class ClassBytesLoader {

    /**
     * @param className of the class which data need to be extracted.
     * @return byte array containing the data associated to the .class file related to the qualified name passed as an argument.
     * @throws IOException
     */
    public static byte[] loadClassBytes(String className) throws IOException {
        int size;
        byte[] classBytes;
        InputStream is;
        String fileSeparator = System.getProperty("file.separator");

        String fileName = className.replace('.', fileSeparator.charAt(0));
        fileName = fileName + ".class";

        // Search for the class in the CLASSPATH
        is = ClassLoader.getSystemResourceAsStream(fileName);
        if (is != null) {
            size = is.available();

            classBytes = new byte[size];

            is.read(classBytes);
            is.close();

            return classBytes;
        } else {
            classBytes = CustomClassLoader.loadClassBytes(className);
            if (classBytes == null) {
                throw new FileNotFoundException(className);
            } else {
                return classBytes;
            }
        }

    }
}