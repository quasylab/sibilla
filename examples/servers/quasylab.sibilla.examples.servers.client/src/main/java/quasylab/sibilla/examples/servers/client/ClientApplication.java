package quasylab.sibilla.examples.servers.client;

import org.apache.commons.math3.random.AbstractRandomGenerator;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.server.NetworkInfo;
import quasylab.sibilla.core.server.client.ClientSimulationEnvironment;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.server.util.SSLUtils;
import quasylab.sibilla.core.simulator.DefaultRandomGenerator;

import java.io.Serializable;

public class ClientApplication implements Serializable {

    public final static int SAMPLINGS = 100;
    public final static double DEADLINE = 600;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int REPLICA = 10;

    private static final AbstractRandomGenerator RANDOM_GENERATOR = new DefaultRandomGenerator();
    private static NetworkInfo MASTER_SERVER_INFO = new NetworkInfo(NetworkUtils.getLocalIp(), 10001,
            TCPNetworkManagerType.SECURE);

    public static void main(String[] argv) throws Exception {

        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("clientKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("clientPass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("clientTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("clientPass");

        SEIRModelDefinition modelDefinition = new SEIRModelDefinition();


        ClientSimulationEnvironment<PopulationState> client = new ClientSimulationEnvironment<PopulationState>(
                RANDOM_GENERATOR, modelDefinition, modelDefinition.createModel(), modelDefinition.state(), SEIRModelDefinition.getCollection(SAMPLINGS, DEADLINE),
                REPLICA, DEADLINE, MASTER_SERVER_INFO);

    }

}
