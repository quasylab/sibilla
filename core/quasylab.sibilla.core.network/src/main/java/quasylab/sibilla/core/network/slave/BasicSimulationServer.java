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

package quasylab.sibilla.core.network.slave;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.network.ComputationResult;
import quasylab.sibilla.core.network.HostLoggerSupplier;
import quasylab.sibilla.core.network.NetworkInfo;
import quasylab.sibilla.core.network.NetworkTask;
import quasylab.sibilla.core.network.communication.TCPNetworkManager;
import quasylab.sibilla.core.network.communication.TCPNetworkManagerType;
import quasylab.sibilla.core.network.compression.Compressor;
import quasylab.sibilla.core.network.loaders.CustomClassLoader;
import quasylab.sibilla.core.network.master.MasterCommand;
import quasylab.sibilla.core.network.serialization.ComputationResultSerializer;
import quasylab.sibilla.core.network.serialization.Serializer;
import quasylab.sibilla.core.network.serialization.SerializerType;
import quasylab.sibilla.core.network.util.NetworkUtils;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Represent a simple server that executes the simulations passed by a master server
 *
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class BasicSimulationServer implements SimulationServer {

    /**
     * Class logger.
     */
    protected Logger LOGGER;

    /**
     * Slave server related network manager type
     */
    private final TCPNetworkManagerType networkManagerType;

    /**
     * Tasks execution related thread executor.
     */
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    /**
     * Master communications related thread executor.
     */
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();

    /**
     * Used by the slave server to manage incoming simulations.
     */
    private int simulationPort;

    /**
     * Slave server network communication related info.
     */
    protected NetworkInfo localServerInfo;

    protected Serializer serializer;


    /**
     * Creates a simulation server with the given network manager type
     *
     * @param networkManagerType type of the network manager
     */
    public BasicSimulationServer(TCPNetworkManagerType networkManagerType, SerializerType serializerType) {
        this.serializer = Serializer.getSerializer(serializerType);
        this.LOGGER = HostLoggerSupplier.getInstance().getLogger();
        this.networkManagerType = networkManagerType;
        LOGGER.info(String.format("Creating a new BasicSimulationServer that uses: [%s - %s]",
                this.networkManagerType.getClass(), this.networkManagerType.name()));
    }

    @Override
    public void start(int port) {
        try {
            this.simulationPort = port;
            this.localServerInfo = new NetworkInfo(NetworkUtils.getLocalAddress(), this.simulationPort, this.networkManagerType);
            LOGGER.info(String.format("The BasicSimulationServer will accept simulation requests on port [%d]", this.simulationPort));
            this.startSimulationServer();
        } catch (SocketException e) {
            LOGGER.severe(String.format("[%s] Network interfaces exception", e.getMessage()));
        }
    }

    /**
     * Starts a simulation server
     */
    private void startSimulationServer() {
        try {
            ServerSocket serverSocket = TCPNetworkManager.createServerSocket(networkManagerType, simulationPort);
            LOGGER.info(String.format("The BasicSimulationServer is now listening for servers on port: [%d]", simulationPort));
            while (true) {
                Socket socket = serverSocket.accept();
                connectionExecutor.execute(() -> manageNewMaster(socket));
            }
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the server socket startup", e.getMessage()));
        }
    }

    /**
     * Manages the messages that come from the master server
     *
     * @param socket socket where the server listens for master messages
     */
    private void manageNewMaster(Socket socket) {

        try {
            TCPNetworkManager master = TCPNetworkManager.createNetworkManager(networkManagerType, socket);

            AtomicBoolean masterIsActive = new AtomicBoolean(true);

            Map<MasterCommand, Runnable> map = Map.of(
                    MasterCommand.PING, () -> respondPingRequest(master),
                    MasterCommand.INIT, () -> loadModelClass(master),
                    MasterCommand.TASK, () -> handleTaskExecution(master),
                    MasterCommand.CLOSE_CONNECTION, () -> closeConnectionWithMaster(masterIsActive, master));
            while (masterIsActive.get()) {
                MasterCommand request = (MasterCommand) serializer.deserialize(master.readObject());
                LOGGER.info(String.format("[%s] command received by master: %s", request, master.getNetworkInfo().toString()));
                map.getOrDefault(request, () -> {
                    throw new ClassCastException("Command received from master server wasn't expected.");
                }).run();
            }

        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during master communication", e.getMessage()));
        }
    }


    /**
     * Closes the connection with the master server
     *
     * @param masterActive boolean that tells if the master is active or not
     * @param master       server of the master
     */
    private void closeConnectionWithMaster(AtomicBoolean masterActive, TCPNetworkManager master) {
        try {
            String modelName = (String) serializer.deserialize(master.readObject());
            LOGGER.info(String.format("[%s] Model name read to be deleted by master: %s", modelName, master.getNetworkInfo().toString()));
            masterActive.set(false);
            CustomClassLoader.removeClassBytes(modelName);
            LOGGER.info(String.format("[%s] Model deleted off the class loader", modelName));

            master.writeObject(serializer.serialize(SlaveCommand.CLOSE_CONNECTION));
            LOGGER.info(String.format("[%s] command sent to the master: %s", SlaveCommand.CLOSE_CONNECTION,
                    master.getNetworkInfo().toString()));

            master.closeConnection();
            LOGGER.info(String.format("Slave closed the connection with master: %s", master.getNetworkInfo().toString()));
        } catch (ClassCastException e) {
            LOGGER.severe(String.format("[%s] Message cast failure during connection closure - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the connection closure - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        }
    }

    /**
     * Loads the model class in the memory with the CustomClassLoader
     *
     * @param master server of the master
     */
    private void loadModelClass(TCPNetworkManager master) {
        try {
            String modelName = (String) serializer.deserialize(master.readObject());
            LOGGER.info(String.format("[%s] Model name read by master: %s", modelName, master.getNetworkInfo().toString()));
            byte[] myClass = master.readObject();
            CustomClassLoader.defClass(modelName, myClass);
            String classLoadedName = Class.forName(modelName).getName();
            LOGGER.info(String.format("[%s] Class loaded with success", classLoadedName));
            master.writeObject(serializer.serialize(SlaveCommand.INIT_RESPONSE));
            LOGGER.info(String.format("[%s] command sent to the master: %s", SlaveCommand.INIT_RESPONSE,
                    master.getNetworkInfo().toString()));
        } catch (ClassNotFoundException e) {
            LOGGER.severe(String.format("[%s] The simulation model was not loaded with success - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the simulation model loading - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        }
    }

    /**
     * Handles the simulation execution sent by the server and sends its results to the master
     *
     * @param master server of the master
     */

    private void handleTaskExecution(TCPNetworkManager master) {
        try {
            NetworkTask<?> networkTask = (NetworkTask<?>) serializer.deserialize(master.readObject());
            List<? extends SimulationTask<?>> tasks = networkTask.getTasks();
            LinkedList<Trajectory<?>> results = new LinkedList<>();
            CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.size()];

            Model model = networkTask.getTasks().get(0).getUnit().getModel();

            for (int i = 0; i < tasks.size(); i++) {
                futures[i] = CompletableFuture.supplyAsync(tasks.get(i), taskExecutor);
            }
            CompletableFuture.allOf(futures).join();
            for (SimulationTask<?> task : tasks) {
                results.add(task.getTrajectory());
            }
            master.writeObject(Compressor.compress(ComputationResultSerializer.serialize(new ComputationResult(results), model)));
            LOGGER.info(String.format("Computation's results have been sent to the server - %s",
                    master.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the task handling - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        }
    }

    /**
     * Responds to a ping request from the master
     *
     * @param master server of the master
     */
    private void respondPingRequest(TCPNetworkManager master) {
        try {
            master.writeObject(serializer.serialize(SlaveCommand.PONG));
            LOGGER.info(String.format("Ping request answered, it was sent by the master: %s",
                    master.getNetworkInfo().toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("[%s] Network communication failure during the ping response - Master: %s", e.getMessage(), master.getNetworkInfo().toString()));
        }
    }

}