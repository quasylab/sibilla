package quasylab.sibilla.core.simulator;

import java.io.IOException;

import quasylab.sibilla.core.simulator.SimulationServer;
import quasylab.sibilla.core.simulator.pm.PopulationState;

public class TestServer {
    public static void main(String[] argv) {
        SimulationServer<PopulationState> server1 = new SimulationServer<>();
        SimulationServer<PopulationState> server2 = new SimulationServer<>();
        SimulationServer<PopulationState> server3 = new SimulationServer<>();
            new Thread(()-> {
                try {
                    server1.start(8080);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
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