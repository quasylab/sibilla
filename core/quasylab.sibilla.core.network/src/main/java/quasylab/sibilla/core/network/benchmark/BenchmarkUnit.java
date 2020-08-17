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

package quasylab.sibilla.core.network.benchmark;

import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class BenchmarkUnit {

    private int executedRuns = 0;
    private String fileName;
    private String dirName;

    public BenchmarkUnit(String dirName, String fileName, String extension, String mainLabel, String timeLabel, String... otherLabels) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss_a");
        String strDate = dateFormat.format(date);

        this.dirName = dirName;
        this.fileName = String.format("%s [%s].%s", fileName, strDate, extension);

        try {
            BufferedWriter fileWriter = getFileWriter();
            fileWriter.write(String.format("repetition_%s", mainLabel));
            fileWriter.write(String.format(",%stime_%s", timeLabel, mainLabel));
            for (String label : otherLabels) {
                fileWriter.write(String.format(",%s_%s", label, mainLabel));
            }
            fileWriter.newLine();
            fileWriter.close();
        } catch (
                Exception ioException) {
            ioException.printStackTrace();
        }
    }

    public BenchmarkUnit(String dirName, String fileName, String extension, String mainLabel, List<String> labels) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss_a");
        String strDate = dateFormat.format(date);

        this.dirName = dirName;
        this.fileName = String.format("%s [%s].%s", fileName, strDate, extension);

        try {
            BufferedWriter fileWriter = getFileWriter();
            fileWriter.write(String.format("repetition_%s", mainLabel));
            for (String label : labels) {
                fileWriter.write(String.format(",%s_%s", label, mainLabel));
            }
            fileWriter.newLine();
            fileWriter.close();
        } catch (
                Exception ioException) {
            ioException.printStackTrace();
        }
    }

    public synchronized void run(Callable<List<Double>> e) {
        try {
            BufferedWriter fileWriter = getFileWriter();
            fileWriter.write(String.valueOf(executedRuns++));
            writeValuesFromCallable(fileWriter, e);
            fileWriter.newLine();
            fileWriter.close();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

    public synchronized void run(Callable<List<Double>>... e) {
        try {
            BufferedWriter fileWriter = getFileWriter();
            fileWriter.write(String.valueOf(executedRuns++));
            for (Callable<List<Double>> callable : e) {
                writeValuesFromCallable(fileWriter, callable);
            }
            fileWriter.newLine();
            fileWriter.close();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

    private BufferedWriter getFileWriter() throws IOException {
        File dir = new File(this.dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, this.fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return new BufferedWriter(new FileWriter(file, true));
    }

    private void writeValuesFromCallable(BufferedWriter fileWriter, Callable<List<Double>> callable) throws
            Exception {
        StopWatch watch = new StopWatch();
        watch.start();
        List<Double> otherData = callable.call();
        watch.stop();
        double nanoValue = watch.getNanoTime();
        double secValue = nanoValue / (Math.pow(10, 9));
        fileWriter.write("," + secValue);
        for (Double n : otherData) {
            fileWriter.write(",");
            fileWriter.write(String.valueOf(n));
        }
    }
}