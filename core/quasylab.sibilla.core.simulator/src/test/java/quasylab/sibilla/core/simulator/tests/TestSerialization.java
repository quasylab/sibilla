package quasylab.sibilla.core.simulator.tests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Function;
import java.io.Serializable;

import quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import quasylab.sibilla.core.simulator.NetworkTask;
import quasylab.sibilla.core.simulator.SimulationTask;
import quasylab.sibilla.core.simulator.Trajectory;
import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;

public class TestSerialization {
    public final static int S = 0;
	public final static int E = 1;
	public final static int I = 2;
	public final static int R = 3;
	
	public final static int INIT_S = 99;
	public final static int INIT_E = 0;
	public final static int INIT_I = 1;
	public final static int INIT_R = 0;
	public final static double N = INIT_S+INIT_E+INIT_I+INIT_R;
	
	public final static double LAMBDA_E = 1;
	public final static double LAMBDA_I = 1/3.0;
    public final static double LAMBDA_R = 1/7.0;
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        PopulationRule rule_S_E = new ReactionRule(
            "S->E", 
            new Specie[] { new Specie(S), new Specie(I)} , 
            new Specie[] { new Specie(E), new Specie(I)},  
            (Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(S)*LAMBDA_E*(s.getOccupancy(I)/N)); 
    
    PopulationRule rule_E_I = new ReactionRule(
            "E->I",
            new Specie[] { new Specie(E) },
            new Specie[] { new Specie(I) },
            (Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(E)*LAMBDA_I
    );
    
    PopulationRule rule_I_R = new ReactionRule(
            "I->R",
            new Specie[] { new Specie(I) },
            new Specie[] { new Specie(R) },
            (Function<PopulationState, Double> & Serializable) s -> s.getOccupancy(I)*LAMBDA_R
    );
    
    PopulationModel f = new PopulationModel( 
            initialState(),
            rule_S_E,
            rule_E_I,
            rule_I_R
    ); 

    SimulationTask<PopulationState> task = new SimulationTask<>(new DefaultRandomGenerator(), f, 600);
    NetworkTask<PopulationState> ntask = new NetworkTask<>(task,1);
    FileOutputStream fos = new FileOutputStream("test-serialization-task.data");
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(ntask);
    oos.flush();
    oos.close();
    FileInputStream fis = new FileInputStream("test-serialization-task.data");
    ObjectInputStream ois = new ObjectInputStream(fis);
    NetworkTask<PopulationState> task2 = (NetworkTask<PopulationState>) ois.readObject();
    ois.close();
    System.out.println(task2);
    Trajectory<PopulationState> trajectory = task2.getTask().get();
    FileOutputStream fos2 = new FileOutputStream("test-serialization-trajectory.data");
    ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
    oos2.writeObject(trajectory);
    oos2.flush();
    oos2.close();
    FileInputStream fis2 = new FileInputStream("test-serialization-trajectory.data");
    ObjectInputStream ois2 = new ObjectInputStream(fis2);
    Trajectory<PopulationState> trajectory2 = (Trajectory<PopulationState>) ois2.readObject();
    ois2.close();
    System.out.println(trajectory2);
    }

	public static PopulationState initialState() {
		return new PopulationState( new int[] { INIT_S, INIT_E, INIT_I, INIT_R } );
	}
}