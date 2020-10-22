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

package it.unicam.quasylab.sibilla.core.network.util;

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
