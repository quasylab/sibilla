package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class MonitoringClient {
    private static final Logger LOGGER = Logger.getLogger(MonitoringClient.class.getName());

    private ServerInfo LOCAL_MONITOR_INFO;
    private ServerInfo REMOTE_MONITOR_INFO;
    private HashMap<String, MasterState> mastersInfo;

    public MonitoringClient(int localPort, ServerInfo remoteMonitoringServer) {
        this.LOCAL_MONITOR_INFO = new ServerInfo(NetworkUtils.getLocalIp(), localPort, remoteMonitoringServer.getType());
        this.REMOTE_MONITOR_INFO = remoteMonitoringServer;
        LOGGER.info(String.format("Creating a monitoring client that will listen for updates on port [%d] from server - %s", LOCAL_MONITOR_INFO.getPort(), REMOTE_MONITOR_INFO.toString()));
        this.subscribe();
        new Thread(() -> this.listenForUpdates()).start();
    }

    private void listenForUpdates() {

        try {
            ServerSocket serverSocket = new ServerSocket(LOCAL_MONITOR_INFO.getPort());
            while (true) {
                Socket socket = serverSocket.accept();
                listenServer(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void listenServer(Socket socket) {
        try {
            TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) LOCAL_MONITOR_INFO.getType(), socket);
            Command received = (Command) ObjectSerializer.deserializeObject(networkManager.readObject());
            LOGGER.info(String.format("[%s] command received from the server - %s", received, networkManager.getServerInfo().toString()));
            if (received.equals(Command.MONITORING_UPDATE)) {
                this.mastersInfo = (HashMap<String, MasterState>) ObjectSerializer.deserializeObject(networkManager.readObject());
                LOGGER.info(String.format("CURRENT UPDATES: [%s]", this.mastersInfoToString(mastersInfo)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mastersInfoToString(Map<String, MasterState> infos) {
        AtomicReference<String> info = new AtomicReference<>("");
        infos.keySet().stream().forEach((master) -> {
            info.set(info + master + "\n" + infos.get(master).toString());
        });
        return info.get();
    }

    private void subscribe() {
        try {
            TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(REMOTE_MONITOR_INFO);
            networkManager.writeObject(ObjectSerializer.serializeObject(Command.MONITORING_SUBSCRIBE));
            networkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_MONITOR_INFO));
            LOGGER.info(String.format("I have subscribed to the server - %s", networkManager.getServerInfo().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe() {
        try {
            TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(REMOTE_MONITOR_INFO);
            networkManager.writeObject(ObjectSerializer.serializeObject(Command.MONITORING_UNSUBSCRIBE));
            networkManager.writeObject(ObjectSerializer.serializeObject(LOCAL_MONITOR_INFO));
            LOGGER.info(String.format("I have unsubscribed from the server - %s", networkManager.getServerInfo().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
