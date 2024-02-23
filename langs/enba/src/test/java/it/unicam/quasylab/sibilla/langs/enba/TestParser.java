package it.unicam.quasylab.sibilla.langs.enba;

import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.expressions.ExpressionContext;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.mutations.AgentDelta;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.Agent;
import it.unicam.quasylab.sibilla.core.models.carma.targets.commons.states.AgentState;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.DataOrientedPopulationModel;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.BroadcastRule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.Rule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.UnicastRule;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.randomgenerators.SplittableRandomGenerator;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.InputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.dopm.rules.transitions.OutputTransition;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.ENBAModel;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.InputAction;
import it.unicam.quasylab.sibilla.core.models.carma.targets.enba.processes.actions.OutputAction;
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

    @Test
    public void testNonExistentSpecies() throws ModelGenerationException {
        String code = """
                species R{k: integer};
                
                channel red{k: integer};
                
                process B {
                    red*<1>[true]!.R[k=k] + red<1>[true]!.R[k=k]
                }
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testDuplicateActionError() throws ModelGenerationException {
        String code = """
                species R{k: integer};
                
                channel red{k: integer};
                
                process R {
                    red*<1>[true]!.R[k=k] + red<1>[true]!.R[k=k]
                }
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testIncompatibleChannelError() throws ModelGenerationException {
        String code = """
                species R{k: integer};
                
                channel red{k: integer, v: boolean};
                
                process R {
                    red*<1>[true]!.R[k=k]
                }
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testParsingProcesses() throws ModelGenerationException {
        String code = """
                species R{k: integer};
                species B{k: integer};
                
                channel red{k: integer};
                
                process R {
                    [k>0] red<k + 2>[k == 5]!.R[k=k]
                        : red*<k*2>[!(k == 3)]!.R[k=k]
                }
                
                process B {
                    [k>4] red<k + 2>[k == 3]?.R[k=k]
                        : red*<k*2>[!(k == 3)]?.R[k=k]
                }
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertTrue(generator.validate());
        ENBAModel model = generator.getModelDefinition().getModel();

        OutputAction output1 = model.getProcesses().get(0).getUnicastOutputs().get("red").get(0);
        assertTrue(output1.channel().equals("red"));
        assertTrue(output1.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        assertFalse(output1.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertTrue(output1.rate().apply(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)) == 4);
        assertTrue(output1.receiverPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(5)),null)));
        assertFalse(output1.receiverPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        Agent post = output1.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post.species() == 0);
        assertTrue(post.values().get("k").intOf() == 0);

        OutputAction output2 = model.getProcesses().get(0).getBroadcastOutputs().get("red").get(0);
        assertTrue(output2.channel().equals("red"));
        assertFalse(output2.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        assertTrue(output2.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertTrue(output2.rate().apply(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)) == 6);
        assertFalse(output2.receiverPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertTrue(output2.receiverPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        Agent post1 = output2.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post1.species() == 0);
        assertTrue(post1.values().get("k").intOf() == 0);

        InputAction input1 = model.getProcesses().get(1).getUnicastInputs().get("red").get(0);
        assertTrue(input1.channel().equals("red"));
        assertTrue(input1.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(5)),null)));
        assertFalse(input1.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertTrue(input1.probability().apply(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)) == 4);
        assertTrue(input1.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertFalse(input1.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        Agent post3 = input1.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post3.species() == 0);
        assertTrue(post3.values().get("k").intOf() == 0);

        InputAction input2 = model.getProcesses().get(1).getBroadcastInputs().get("red").get(0);
        assertTrue(input2.channel().equals("red"));
        assertFalse(input2.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(5)),null)));
        assertTrue(input2.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertTrue(input2.probability().apply(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)) == 6);
        assertFalse(input2.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertTrue(input2.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        Agent post4 = input2.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post4.species() == 0);
        assertTrue(post4.values().get("k").intOf() == 0);


    }

    @Test
    public void testDopmConversion() throws ModelGenerationException {
        String code = """
                species R{k: integer};
                species B{k: integer};
                
                channel red{k: integer};
                
                process R {
                    [k>0] red<k + 2>[k == 5]!.R[k=k]
                        : red*<k*2>[!(k == 3)]!.R[k=k]
                }
                
                process B {
                    [k>4] red<k + 2>[k == 3]?.R[k=k]
                        : red*<k*2>[!(k == 3)]?.R[k=k]
                }
                """;
        ENBAModelGenerator generator = new ENBAModelGenerator(code);
        assertTrue(generator.validate());
        ENBAModel model = generator.getModelDefinition().getModel();
        DataOrientedPopulationModel dopm = model.getDopm();

        Rule rule1 = dopm.getRules().get(0);
        assertTrue(rule1 instanceof BroadcastRule);
        OutputTransition outputTransition1 = rule1.getOutput();
        assertTrue(outputTransition1.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertFalse(outputTransition1.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(1)),null)));
        assertTrue(outputTransition1.rate().apply(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)) == 4);
        Agent post5 = outputTransition1.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post5.species() == 0);
        assertTrue(post5.values().get("k").intOf() == 0);

        InputTransition inputTransition1 = rule1.getInputs().get(0);
        assertFalse(inputTransition1.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertFalse(inputTransition1.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(5)),null)));
        assertTrue(inputTransition1.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(4)),null)));
        assertFalse(inputTransition1.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertTrue(inputTransition1.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));

        Agent post6 = inputTransition1.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post6.species() == 0);
        assertTrue(post6.values().get("k").intOf() == 0);

        Rule rule2 = dopm.getRules().get(1);
        assertTrue(rule2 instanceof UnicastRule);
        OutputTransition outputTransition2 = rule2.getOutput();
        assertTrue(outputTransition2.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));
        assertFalse(outputTransition2.predicate().test(0,new ExpressionContext(Map.of("k", new SibillaInteger(0)),null)));
        assertTrue(outputTransition2.rate().apply(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)) == 4);
        Agent post7 = outputTransition2.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post7.species() == 0);
        assertTrue(post7.values().get("k").intOf() == 0);

        InputTransition inputTransition2 = rule2.getInputs().get(0);
        assertFalse(inputTransition2.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertTrue(inputTransition2.predicate().test(1,new ExpressionContext(Map.of("k", new SibillaInteger(5)),null)));
        assertTrue(inputTransition2.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(3)),null)));
        assertFalse(inputTransition2.senderPredicate().test(new ExpressionContext(Map.of("k", new SibillaInteger(2)),null)));

        Agent post8 = inputTransition2.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("k", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));
        assertTrue(post8.species() == 0);
        assertTrue(post8.values().get("k").intOf() == 0);
    }

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
