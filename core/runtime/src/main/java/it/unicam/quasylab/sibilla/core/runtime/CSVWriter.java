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

package it.unicam.quasylab.sibilla.core.runtime;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to store data in a CSV file.
 */
public class CSVWriter {

    private final File outputFolder;
    private final String prefix;
    private final String postfix;

    /**
     * Creates a CSVWriter that saves data in the given output folder. The name of saved files is prefixed
     * and postfixed with the given strings. If <code>outputFolder</code> does not exists it is created,
     * while if it is not a directory, an {@link IllegalArgumentException} is thrown.
     *
     * @param outputFolder folder where CSV are saved
     * @param prefix string that is placed at the beginning of the name of each saved file
     * @param postfix string that is placed at the end of the name of each saved file
     * @throws IllegalArgumentException if the given file is not a directory
     */
    public CSVWriter(File outputFolder, String prefix, String postfix) throws IOException {
        if (!outputFolder.exists()) {
            Files.createDirectory(outputFolder.toPath());
        }
        if (!outputFolder.isDirectory()) {
            //TO LOCALIZE
            throw new IllegalArgumentException(outputFolder.getAbsolutePath()+" is not a directory!");
        }
        this.outputFolder = outputFolder;
        this.postfix = postfix;
        this.prefix = prefix;
    }

    /**
     * Creates a CSVWriter that saves data in the given output folder. The name of saved files is prefixed
     * and postfixed with the given strings. If <code>outputFolder</code> does not exists it is created,
     * while if it is not a directory, an {@link IllegalArgumentException} is thrown.
     *
     * @param outputFolder folder where CSV are saved
     * @param prefix string that is placed at the beginning of the name of each saved file
     * @param postfix string that is placed at the end of the name of each saved file
     * @throws IllegalArgumentException if the given file is not a directory
     * @throws IOException if the outputFolder name is <code>null</code>
     */
    public CSVWriter(String outputFolder, String prefix, String postfix) throws IOException {
        this(new File(outputFolder), prefix, postfix);
    }


    /**
     * Creates a CSVWriter that saves data in the given output folder. The name of saved files is prefixed
     * and postfixed with the given strings. If <code>outputFolder</code> does not exists it is created,
     * while if it is not a directory, an {@link IllegalArgumentException} is thrown. Empty strings are used
     * as prefix and postfix of file names.
     *
     * @param outputFolder folder where CSV are saved
     * @throws IllegalArgumentException if the given file is not a directory
     * @throws IOException if the outputFolder name is <code>null</code>
     */
    public CSVWriter(String outputFolder) throws IOException {
        this(outputFolder, "", "");
    }

    /**
     * This is a utility method that permits generating the row of a CSV file from an array of double.
     *
     * @param row data in the row
     * @return the row of a CSV file resulting from the given array of double
     */
    public static String getCSVRow(double[] row) {
        return Arrays.stream(row).boxed().map(d -> String.format(java.util.Locale.US,"%f", d)).collect(Collectors.joining(", "));
        //return Arrays.stream(row).boxed().map(d -> String.format("%f",d)).collect(Collectors.joining(", "));
    }

    /**
     * Returns the String containing the CSV document associated with the given data.
     *
     * @param data data to save in CSV format
     * @return the String containing the CSV document associated with the given data.
     */
    public static String getCSVString(double[][] data) {
        return Arrays.stream(data)
                .map(CSVWriter::getCSVRow)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns the String containing the CSV document associated with the given table.
     *
     * @param table data to save in CSV format
     * @return the String containing the CSV document associated with the given table.
     */
    public static String getCSVStringFromTable(Table table){
        StringBuilder csvString = new StringBuilder();

        List<String> titles = table.columnNames();
        int numberOfColumn = table.columnCount();
        int numberOfRow = table.rowCount();


        for (int i = 0; i < numberOfColumn-1; i++) {
            csvString.append(titles.get(i)).append(",");
        }

        csvString.append(titles.get(numberOfColumn-1)).append("\n");

        for (int i = 0; i < numberOfRow; i++) {
            Row row = table.row(i);
            for (int j = 0; j < numberOfColumn-1; j++) {
                csvString.append(row.getDouble(j)).append(",");
            }
            csvString.append(row.getDouble(numberOfColumn-1)).append("\n");
        }
        return csvString.toString();
    }


    /**
     * Prints the CSV document resulting from the given data with the given {@link PrintWriter}.
     *
     * @param pw object used to print the CSV
     * @param data data to print in CSV format
     */
    public static void writeCSV(PrintWriter pw, double[][] data) {
        for (double[] row: data) {
            pw.println(getCSVRow(row));
        }
    }

    /**
     * Saves the data with a given name. The generated file will be placed in {@link #getOutputFolder()}.
     * The name of the file will be <code>getPrefix()+name+getPostfix()+".csv"</code>.
     *
     *
     * @param name name of the data to save
     * @param data data to save in CSV format
     * @throws IOException if an error occurred while saving data
     */
    public void write(String name, double[][] data) throws IOException {
        File output = new File(outputFolder,prefix+name+postfix+".csv");
        PrintWriter writer = new PrintWriter(output);
        writeCSV(writer, data);
        writer.close();
    }


    /**
     * Saves the table with a given name. The generated file will be placed in {@link #getOutputFolder()}.
     * The name of the file will be <code>getPrefix()+name+getPostfix()+".csv"</code>.
     *
     *
     * @param name name of the data to save
     * @param table data to save in CSV format
     * @throws IOException if an error occurred while saving data
     */
    public void write(String name, Table table) throws IOException {
        File output = new File(outputFolder,prefix+name+postfix+".csv");
        FileWriter writer = new FileWriter(output);
        writer.write(getCSVStringFromTable(table));
        writer.close();
    }

    /**
     * Saves all the data in the map.
     *
     * @param data data to save in CSV format
     * @throws IOException if an error occurred while saving data
     */
    public void write(Map<String, double[][]> data) throws IOException {
        for (Map.Entry<String, double[][]> entry : data.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the output folder where this writer saves files.
     *
     * @return the output folder where this writer saves files
     */
    public File getOutputFolder() {
        return outputFolder;
    }

    /**
     * Returns the string that is used as a prefix of the saved files by this writer.
     * @return the string that is used as a prefix of the saved files by this writer.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the string that is used as a postfix of the save files by this writer.
     * @return the string that is used as a postfix of the save files by this writer
     */
    public String getPostfix() {
        return postfix;
    }
}
