package quasylab.sibilla.core.simulator.newserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.io.IOException;

public class ServerSimulationEnvironment {

    private ServerSocket masterServerSocket;
    private final DatagramSocket discoverySocket;
    private List<ServerInfo> server;

    public ServerSimulationEnvironment() throws SocketException {
        discoverySocket = new DatagramSocket(10000);
    }

    public void startDiscoveryServer() {
        while (true) {
            try {
                byte[] buf = new byte[4096];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                discoverySocket.receive(p);
                String s = new String(p.getData());
                Arrays.stream(s.split(",")).forEach(info -> {int port = Integer.valueOf(info.split(";")[0];
                SerializationType type = SerializationType.valueOf(info.split(";")[1]);server.add(new ServerInfo(p.getAddress(), port, type))))})
            } catch (IOException e) {
                continue;
            }
        }
    }

    public void start(int port) {
        while (true) {
            try {
                Socket socket = masterServerSocket.accept();
            } catch (IOException e) {
                continue;
            }
        }
    }

}
