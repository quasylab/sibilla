package quasylab.sibilla.core.simulator.newserver;

import quasylab.sibilla.core.simulator.network.TCPNetworkManager;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.serialization.ObjectSerializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import quasylab.sibilla.core.util.NetworkUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

public class MonitoringServer implements PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(MonitoringServer.class.getName());

    private HashSet<ServerInfo> monitoringClients;
    private final ServerInfo LOCAL_MONITOR_INFO;
    private HashMap<String, MasterState> mastersInfo;

    public MonitoringServer(int monitoringPort, TCPNetworkManagerType monitoringNetworkManager) {
        LOCAL_MONITOR_INFO = new ServerInfo(NetworkUtils.getLocalIp(), monitoringPort, monitoringNetworkManager);
        mastersInfo = new HashMap<>();
        monitoringClients = new HashSet<>();
        LOGGER.info(String.format("Creating a monitoring server that will listen for subscribers on port: [%d]", LOCAL_MONITOR_INFO.getPort()));
        new Thread(() -> this.listenForSubscribers()).start();
        // new Thread(() -> this.updateSubscribers()).start();
    }

    private void updateSubscribers() {
        //  while (true) {
        monitoringClients.stream().forEach(this::updateClient);
        LOGGER.info(String.format("Subscribers have been updated"));
        // Thread.sleep(5000);
        //   }
    }

    private void updateClient(ServerInfo serverInfo) {
        try {
            TCPNetworkManager networkManager = TCPNetworkManager.createNetworkManager(serverInfo);
            networkManager.writeObject(ObjectSerializer.serializeObject(Command.MONITORING_UPDATE));
            networkManager.writeObject(ObjectSerializer.serializeObject(this.mastersInfo));
            // networkManager.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForSubscribers() {
        try {
            ServerSocket serverSocket = new ServerSocket(LOCAL_MONITOR_INFO.getPort());
            LOGGER.info(String.format("Now listening for new subscribers on port: [%d]", LOCAL_MONITOR_INFO.getPort()));
            while (true) {
                Socket socket = serverSocket.accept();
                this.manageSubscriber(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manageSubscriber(Socket socket) {
        try {
            TCPNetworkManager monitoringNetworkManager = TCPNetworkManager.createNetworkManager((TCPNetworkManagerType) LOCAL_MONITOR_INFO.getType(), socket);
            Map<Command, Runnable> map = Map.of(Command.MONITORING_SUBSCRIBE, () -> subscribeClient(monitoringNetworkManager), Command.MONITORING_UNSUBSCRIBE, () -> unsubscribeClient(monitoringNetworkManager));

            Command command = (Command) ObjectSerializer.deserializeObject(monitoringNetworkManager.readObject());
            LOGGER.info(String.format("[%s] command received by the client - %s", command.toString(), monitoringNetworkManager.getServerInfo().toString()));
            map.getOrDefault(command, () -> {
            }).run();
            // monitoringNetworkManager.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unsubscribeClient(TCPNetworkManager manager) {
        try {
            ServerInfo clientInfo = (ServerInfo) ObjectSerializer.deserializeObject(manager.readObject());
            if (this.monitoringClients.remove(clientInfo)) {
                LOGGER.info(String.format("Subscriber removed - %s", clientInfo.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeClient(TCPNetworkManager manager) {
        try {
            ServerInfo clientInfo = (ServerInfo) ObjectSerializer.deserializeObject(manager.readObject());
            if (this.monitoringClients.add(clientInfo)) {
                LOGGER.info(String.format("New subscriber added - %s", clientInfo.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() instanceof MasterState) {
            mastersInfo.put(evt.getPropertyName(), (MasterState) evt.getNewValue());
        }
        this.updateSubscribers();
    }

}
