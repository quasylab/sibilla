package quasylab.sibilla.core.network.util;

import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.communication.UDPNetworkManagerType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to create startup classes for new masters, slaves and servers.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class StartupUtils {

    /**
     * @param args from the console
     * @return Map containing all the console startup args and the related values
     */
    public static Map<String, String> parseOptions(String[] args) {
        final Map<String, String> options = new HashMap<>();

        String optionArgument = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return null;
                }
                optionArgument = a;
            } else if (optionArgument != null) {
                options.put(optionArgument.substring(1), a);
                optionArgument = null;
            }
        }
        return options;
    }

    /**
     * @param type name of the {@link quasylab.sibilla.core.network.communication.TCPNetworkManagerType} to obtain
     * @return {@link quasylab.sibilla.core.network.communication.TCPNetworkManagerType} related to the name passed as argument
     */
    public static TCPNetworkManagerType TCPNetworkManagerParser(String type) {
        switch (type) {

            case "SECURE":
                return TCPNetworkManagerType.SECURE;


            case "DEFAULT":
            default:
                return TCPNetworkManagerType.DEFAULT;
        }
    }

    /**
     * @param type name of the {@link quasylab.sibilla.core.network.communication.UDPNetworkManagerType} to obtain
     * @return {@link quasylab.sibilla.core.network.communication.UDPNetworkManagerType} related to the name passed as argument
     */
    public static UDPNetworkManagerType UDPNetworkManagerParser(String type) {
        switch (type) {
            case "DEFAULT":
            default:
                return UDPNetworkManagerType.DEFAULT;
        }
    }

}
