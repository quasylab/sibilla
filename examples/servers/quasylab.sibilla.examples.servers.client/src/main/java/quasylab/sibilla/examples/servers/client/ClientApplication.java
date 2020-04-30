package quasylab.sibilla.examples.servers.client;


import org.apache.commons.math3.random.AbstractRandomGenerator;
import quasylab.sibilla.core.server.ServerInfo;
import quasylab.sibilla.core.server.client.ClientSimulationEnvironment;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.util.NetworkUtils;
import quasylab.sibilla.core.server.util.SSLUtils;
import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

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
    private static final String MODEL_NAME = ClientApplication.class.getName();
    private static final ServerInfo MASTER_SERVER_INFO = new ServerInfo(NetworkUtils.getLocalIp(), 10001, TCPNetworkManagerType.SECURE);

    public static void main(String[] argv) throws Exception {

        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("clientKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("clientPass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("clientTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("clientPass");


        ClientModelDefinition def = new ClientModelDefinition();
        StatisticSampling<PopulationState> fiSamp = StatisticSampling.measure("I", SAMPLINGS, DEADLINE,
                ClientModelDefinition::fractionOfI);
        StatisticSampling<PopulationState> frSamp = StatisticSampling.measure("R", SAMPLINGS, DEADLINE,
                ClientModelDefinition::fractionOfR);

        SamplingFunction<PopulationState> sf = new SamplingCollection<>(fiSamp, frSamp);

        ClientSimulationEnvironment<PopulationState> client = new ClientSimulationEnvironment<PopulationState>(
				RANDOM_GENERATOR,
                def.createModel(), def.state( ), sf, REPLICA, DEADLINE, MASTER_SERVER_INFO);

    }


}
