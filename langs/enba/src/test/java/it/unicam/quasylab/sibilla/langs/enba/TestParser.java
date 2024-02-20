package it.unicam.quasylab.sibilla.langs.enba;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.DataOrientedPopulationModel;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.randomgenerators.SplittableRandomGenerator;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.langs.enba.generators.ENBAModelGenerator;
import it.unicam.quasylab.sibilla.langs.enba.generators.exceptions.ModelGenerationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParser {
    @Test
    public void testErrorParsingDuplicateSpeciesValues() throws ModelGenerationException {
        String code = """
                species A {v: integer, v: boolean};
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingDuplicateSpecies() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species A {a: boolean};
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertFalse(generator.validate());
    }

    //TODO test process and channel parsing

    @Test
    public void testParsingSystem() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                system s = A[v=0]#100 | B[v=0,k=true];
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertTrue(generator.validate());
        AgentState s = generator.getModelDefinition().getConfiguration("s").apply(new SplittableRandomGenerator());

        assertTrue(s.getAgents().get(new Agent(0, Map.of("v", new SibillaInteger(0)))) == 100);
        assertTrue(s.getAgents().get(new Agent(1, Map.of("v", new SibillaInteger(0), "k", SibillaBoolean.TRUE))) == 1);

    }
}
