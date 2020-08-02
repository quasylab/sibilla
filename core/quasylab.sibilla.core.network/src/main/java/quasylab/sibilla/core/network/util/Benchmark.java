package quasylab.sibilla.core.network.util;

import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class Benchmark {

    private int executedRuns = 0;
    private String fileName;
    private String dirName;

    public Benchmark(String dirName, String fileName, String extension, String mainLabel, String timeLabel, String... otherLabels) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss_a");
        String strDate = dateFormat.format(date);

        this.dirName = dirName;
        this.fileName = String.format("%s [%s].%s", fileName, strDate, extension);

        try {
            File dir = new File(this.dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, this.fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, true));
            fileWriter.write(String.format("repetition", mainLabel));
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

    public synchronized void run(Callable<List<Double>> e) {
        try {
            File dir = new File(dirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, true));
            fileWriter.write(String.valueOf(executedRuns++));
            fileWriter.write(",");
            StopWatch watch = new StopWatch();
            watch.start();
            List<Double> otherData = e.call();
            watch.stop();
            double nanoValue = watch.getNanoTime();
            double secValue = nanoValue / (Math.pow(10, 9));
            fileWriter.write(String.valueOf(secValue));
            for (Double n : otherData) {
                fileWriter.write(",");
                fileWriter.write(String.valueOf(n));
            }
            fileWriter.newLine();
            fileWriter.close();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }
}
