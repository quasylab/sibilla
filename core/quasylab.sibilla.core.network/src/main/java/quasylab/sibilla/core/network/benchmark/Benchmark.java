package quasylab.sibilla.core.network.benchmark;

import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Benchmark {

    public static void benchmark(int replica, Callable<List<Double>> e, Supplier<String> filenameSupplier) {

        try {
            File dir = new File("./benchmarks");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, filenameSupplier.get());
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < replica; i++) {
                fileWriter.write(String.valueOf(i));
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
                fileWriter.write(";");
                fileWriter.newLine();
            }

            fileWriter.close();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }
}
