
package quasylab.sibilla.examples.agents;

import org.apache.commons.math3.random.RandomGenerator;
import quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.*;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static quasylab.sibilla.examples.agents.RoboticScenarioDefinition.*;

public class RandomisedRobotBehaviour implements AgentBehaviour {

    @Override
    public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState, VariableMapping observations) {
       
    	int rand1 = rg.nextInt(100);
//    	int rand2 = rg.nextInt(100);
        
    	if (observations.get(GOAL_SENSOR)==0.0) {
    		if (rand1<25) {
    			return ChangeDirectionAction.DOWN;
    		}
    		if (rand1>=75) {
    			return ChangeDirectionAction.STAND;
    		}
            if (observations.get(FRONT_SENSOR) == 0) {
                return ChangeDirectionAction.UP;
            }
            if (observations.get(RIGHT_SENSOR) == 0) {
                return ChangeDirectionAction.RIGHT;
            }
            if (observations.get(LEFT_SENSOR) == 0) {
                return ChangeDirectionAction.LEFT;
            }
        } else {
            System.err.println("GOAL!!!!");
        }
        return ChangeDirectionAction.STAND;
    }
}