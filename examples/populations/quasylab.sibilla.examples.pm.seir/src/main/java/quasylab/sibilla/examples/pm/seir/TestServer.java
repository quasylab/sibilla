package quasylab.sibilla.examples.pm.seir;

import java.io.IOException;

import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.server.BasicSimulationServer;
import quasylab.sibilla.core.simulator.server.SimulationServer;

public class TestServer {
    public static void main(String[] argv) {
        SimulationServer<PopulationState> server1 = new BasicSimulationServer<>(SerializationType.FST);
        SimulationServer<PopulationState> server2 = new BasicSimulationServer<>(SerializationType.FST);
        SimulationServer<PopulationState> server3 = new BasicSimulationServer<>(SerializationType.FST);
                new Thread(() -> {try {
                    server1.start(8080);
                } catch (IOException e) {
                    e.printStackTrace();
                }}).start();
            new Thread(()-> {
                try {
                    server2.start(8081);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(()-> {
                try {
                    server3.start(8082);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
    }
}