
package it.unicam.quasylab.sibilla.examples.agents;

import org.apache.commons.math3.random.RandomGenerator;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentAction;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentBehaviour;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;

import static it.unicam.quasylab.sibilla.examples.agents.RoboticScenarioDefinition.*;

public class PrioritisedRobotBehaviour implements AgentBehaviour {


    @Override
    public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState, VariableMapping observations) {

        boolean goRight = currentState.get(DIR_FLAG_VARIABLE)==1.0;

    	if (observations.get(GOAL_SENSOR)==0.0) {
            if (observations.get(FRONT_SENSOR) == 0) {
                return ChangeDirectionAction.UP;
            }
            if (goRight) {
                if (observations.get(RIGHT_SENSOR) == 0) {
                    return ChangeDirectionAction.RIGHT;
                }
                if (observations.get(LEFT_SENSOR) == 0) {
                    return ChangeFlagAction.LEFT_AND_CHANGE_FLAG_ACTION;
                }
            } else {
                if (observations.get(LEFT_SENSOR) == 0) {
                    return ChangeDirectionAction.LEFT;
                }
                if (observations.get(RIGHT_SENSOR) == 0) {
                    return ChangeFlagAction.RIGHT_AND_CHANGE_FLAG_ACTION;
                }
            }
        } else {
            System.err.println("GOAL!!!!");
        }
        return ChangeDirectionAction.STAND;
    }
}