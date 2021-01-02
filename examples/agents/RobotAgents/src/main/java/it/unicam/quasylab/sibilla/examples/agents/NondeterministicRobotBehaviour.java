
package it.unicam.quasylab.sibilla.examples.agents;

import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentAction;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.AgentBehaviour;
import it.unicam.quasylab.sibilla.core.models.quasylab.sibilla.core.models.agents.VariableMapping;
import org.apache.commons.math3.random.RandomGenerator;

import static quasylab.sibilla.examples.agents.RoboticScenarioDefinition.*;

public class NondeterministicRobotBehaviour implements AgentBehaviour {

    private boolean goRight = true;

    @Override
    public AgentAction step(RandomGenerator rg, double now, VariableMapping currentState, VariableMapping observations) {
       

    	if (observations.get(GOAL_SENSOR)==0.0) {
            if (observations.get(FRONT_SENSOR) == 0) {
                return ChangeDirectionAction.UP;
            }
            if (goRight) {
                if (observations.get(RIGHT_SENSOR) == 0) {
                    return ChangeDirectionAction.RIGHT;
                }
                if (observations.get(LEFT_SENSOR) == 0) {
                    this.goRight = false;
                    return ChangeDirectionAction.LEFT;
                }
            } else {
                if (observations.get(LEFT_SENSOR) == 0) {
                    return ChangeDirectionAction.LEFT;
                }
                if (observations.get(RIGHT_SENSOR) == 0) {
                    this.goRight = true;
                    return ChangeDirectionAction.RIGHT;
                }
            }
        } else {
            System.err.println("GOAL!!!!");
        }
        return ChangeDirectionAction.STAND;
    }
}