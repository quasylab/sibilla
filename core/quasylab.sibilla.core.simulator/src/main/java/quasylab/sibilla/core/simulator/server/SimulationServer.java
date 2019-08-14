package quasylab.sibilla.core.simulator.server;

import java.io.IOException;

public interface SimulationServer<S>{
    public void start(int port) throws IOException;
}