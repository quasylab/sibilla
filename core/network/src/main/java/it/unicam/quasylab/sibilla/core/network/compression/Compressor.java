/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package it.unicam.quasylab.sibilla.core.network.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class used to compress and decompress byte arrays containing data.
 * The class operations are based upon the tool GZIP.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class Compressor {

    /**
     * Compresses a byte array.
     *
     * @param decompressedData byte array to be compressed
     * @return compressed byte array
     */
    public static byte[] compress(byte[] decompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(decompressedData.length);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(decompressedData);
            gzipOutputStream.close();
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Decompresses a byte array.
     *
     * @param compressedData byte array to be decompressed
     * @return decompressed byte array
     */
    public static byte[] decompress(byte[] compressedData) {
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
