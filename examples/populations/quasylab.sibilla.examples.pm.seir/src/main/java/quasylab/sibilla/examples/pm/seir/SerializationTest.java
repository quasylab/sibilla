package quasylab.sibilla.examples.pm.seir;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.NetworkSimulationManager;
import quasylab.sibilla.core.simulator.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.SimulationUnit;
import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
import quasylab.sibilla.core.simulator.sampling.SamplePredicate;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;
import quasylab.sibilla.core.simulator.serialization.ClassBytesLoader;
import quasylab.sibilla.core.simulator.serialization.SerializationType;
import quasylab.sibilla.core.simulator.serialization.Serializer;
import quasylab.sibilla.core.simulator.server.ComputationResult;
import quasylab.sibilla.core.simulator.server.ServerInfo;

import java.io.Serializable;

public class SerializationTest {

        public final static int S = 0;
        public final static int E = 1;
        public final static int I = 2;
        public final static int R = 3;

        public final static int INIT_S = 99;
        public final static int INIT_E = 0;
        public final static int INIT_I = 1;
        public final static int INIT_R = 0;
        public final static double N = INIT_S + INIT_E + INIT_I + INIT_R;

        public final static double LAMBDA_E = 1;
        public final static double LAMBDA_I = 1 / 3.0;
        public final static double LAMBDA_R = 1 / 7.0;

        public final static int SAMPLINGS = 1;
        public final static double DEADLINE = 10;
        private static final int REPLICA = 10;

        public static void main(String[] argv) throws Exception {

                PopulationRule rule_S_E = new ReactionRule("S->E", new Specie[] { new Specie(S), new Specie(I) },
                                new Specie[] { new Specie(E), new Specie(I) },
                                s -> s.getOccupancy(S) * LAMBDA_E * (s.getOccupancy(I) / N));

                PopulationRule rule_E_I = new ReactionRule("E->I", new Specie[] { new Specie(E) },
                                new Specie[] { new Specie(I) }, s -> s.getOccupancy(E) * LAMBDA_I);

                PopulationRule rule_I_R = new ReactionRule("I->R", new Specie[] { new Specie(I) },
                                new Specie[] { new Specie(R) }, s -> s.getOccupancy(I) * LAMBDA_R);

                PopulationModel f = new PopulationModel();
                f.addState("init", initialState());
                f.addRule(rule_S_E);
                f.addRule(rule_E_I);
                f.addRule(rule_I_R);

                Serializer server = Serializer.createSerializer(new ServerInfo(InetAddress.getByName("192.168.1.201"),
                                8080, SerializationType.DEFAULT));

                String className = SerializationTest.class.getName();
                byte[] classBytes = ClassBytesLoader.loadClassBytes(className);

                server.writeObject(className);
                server.writeObject(classBytes);

                SimulationUnit<PopulationState> unit = new SimulationUnit<PopulationState>(f, initialState(),
                                SamplePredicate.timeDeadlinePredicate(DEADLINE),
                                (Predicate<? super PopulationState> & Serializable) s -> true);

                RandomGenerator random = new DefaultRandomGenerator();
                List<SimulationTask<PopulationState>> toRun = new ArrayList<>();
                for (int i = 1; i <= REPLICA; i++) {
                        String request = "TASK";
                        server.writeObject(request);
                        toRun.add(new SimulationTask<>(random, unit));
                        NetworkTask<PopulationState> netTask = new NetworkTask<>(toRun);
                        server.writeObject(netTask);

                        ComputationResult<PopulationState> receivedResult = (ComputationResult<PopulationState>) server
                                        .readObject();
                        System.out.println(String.format("The results from the computation have been received, %s",
                                        receivedResult.toString()));
                }

        }

        public static PopulationState initialState() {
                return new PopulationState(new int[] { INIT_S, INIT_E, INIT_I, INIT_R });
        }
}
