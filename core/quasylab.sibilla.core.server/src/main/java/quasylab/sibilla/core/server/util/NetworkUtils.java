package quasylab.sibilla.core.server.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;


public class NetworkUtils {

    /**
     * Returns the local IPV4 address of the machine
     * @return the address
     * @throws SocketException TODO Exception
     */
    public static InetAddress getLocalIp(){
        try {
            return NetworkInterface.networkInterfaces().filter(networkInterface -> {
                try {
                    return !networkInterface.isLoopback() && networkInterface.isUp();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                return false;
            }).findFirst().map(networkInterface -> networkInterface.getInterfaceAddresses()).get().stream().filter(interfaceAddress -> interfaceAddress.getAddress() instanceof Inet4Address).findFirst().get().getAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
