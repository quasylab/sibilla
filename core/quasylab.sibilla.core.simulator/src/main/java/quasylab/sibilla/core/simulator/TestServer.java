package quasylab.sibilla.core.simulator;

import java.io.IOException;

import quasylab.sibilla.core.simulator.SimulationServer;
import quasylab.sibilla.core.simulator.pm.PopulationState;

public class TestServer {
    public static void main(String[] argv) {
        SimulationServer<PopulationState> server = new SimulationServer<>();
        try {
            server.start(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}