package quasylab.sibilla.core.simulator.newserver;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.Model;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.serialization.Serializer;
import quasylab.sibilla.core.simulator.server.ServerInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientSimulationEnvironment<S> {

    private String modelReferenceName;
    private Model<S> modelReference;
    private S modelReferenceInitialState;
    private SamplingFunction<S> modelReferenceSamplingFunction;
    private int replica;
    private double deadline;
    private Serializer masterServerSerializer;
    private RandomGenerator randomGenerator;

    private static final Logger LOGGER = Logger.getLogger(ClientSimulationEnvironment.class.getName());

    public ClientSimulationEnvironment(RandomGenerator random, String modelName, Model<S> model, S initialState,
            SamplingFunction<S> sampling_function, int replica, double deadline, ServerInfo masterServerInfo)
            throws IOException {
        this.modelReferenceName = modelName;
        this.randomGenerator = random;
        this.modelReference = model;
        this.modelReferenceInitialState = initialState;
        this.modelReferenceSamplingFunction = sampling_function;
        this.replica = replica;
        this.deadline = deadline;
        this.masterServerSerializer = Serializer.createSerializer(masterServerInfo);
        LOGGER.info("Client environemnt initialized");
    }

    private void sendSimulationInfo(Serializer targetServer) {
    	
    }

    private String convertIntoJson() {
    	 ObjectMapper mapper = new ObjectMapper();
    }

}
