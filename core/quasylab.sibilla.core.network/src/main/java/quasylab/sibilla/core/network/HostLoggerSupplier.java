package quasylab.sibilla.core.network;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Class that supplies a singleton Logger instance to be used to log all host's activities.
 * The log is done both on file and console.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class HostLoggerSupplier {

    private static HostLoggerSupplier instance;
    private static Logger loggerInstance;


    private HostLoggerSupplier() {
    }

    public static HostLoggerSupplier getInstance(String hostName) {
        if (instance == null) {
            instance = new HostLoggerSupplier();

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss_a");
            String strDate = dateFormat.format(date);

            loggerInstance = Logger.getLogger(String.format("%sLog_%s", hostName, strDate));
            try {
                FileHandler fh = new FileHandler(String.format("./%s_%s.log", hostName, strDate));
                loggerInstance.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (IOException e) {
                System.out.println("The logger won't write on file");
            }
        }
        return instance;
    }

    public static HostLoggerSupplier getInstance() {
        if (instance == null) {
            instance = new HostLoggerSupplier();

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss_a");
            String strDate = dateFormat.format(date);

            loggerInstance = Logger.getLogger(String.format("HostLog_%s", strDate));
            try {
                FileHandler fh = new FileHandler(String.format("./Host_%s.log", strDate));
                loggerInstance.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (IOException e) {
                System.out.println("The logger won't write on file");
            }
        }
        return instance;
    }

    public Logger getLogger() {
        return this.loggerInstance;
    }

}
