package it.unicam.quasylab.sibilla.langs.dopm;

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
import it.unicam.quasylab.sibilla.core.util.values.SibillaBoolean;
import it.unicam.quasylab.sibilla.core.util.values.SibillaInteger;
import it.unicam.quasylab.sibilla.langs.dopm.generators.DataOrientedPopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.dopm.generators.exceptions.ModelGenerationException;
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
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingDuplicateSpecies() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species A {a: boolean};
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleNameDuplicate() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer};
                
                rule A {
                    A[true] -[1]-> B[k=k] |> B[true] -[true: 1]-> A[k=k]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongAgentExpression1() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer};
                
                rule ruleA {
                    A[true] -[1]-> B[k=v] |> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongAgentExpression2() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=0] |> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongAgentExpression3() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v] |> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongAgentExpression4() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=0] |> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongPredicate1() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=false] |> B[k>0] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongPredicate2() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=false] |> B[v] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleWrongPredicate3() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=false] |> B[a == 0] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingSystem1() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                system s = A[v=0] | B[v=0,k=true] | C[v=0];
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingSystem2() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                system s = A[v=0] | B[v=0,k=0];
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingSystem3() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                system s = A[v=0] | B[v=#A[true],k=true];
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        generator.validate();
        assertFalse(generator.validate());
    }

    @Test
    public void testErrorParsingRuleBroadcast() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=true] *|> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertTrue(generator.validate());
        DataOrientedPopulationModel model = generator.getPopulationModelDefinition().getModel();
        Rule ruleA = model.getRules().get(0);
        assertTrue(ruleA instanceof BroadcastRule);
    }

    @Test
    public void testErrorParsingRuleUnicast() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                rule ruleA {
                    A[true] -[1]-> B[v=v,k=true] |> B[true] -[true: 1]-> A[v=v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertTrue(generator.validate());
        DataOrientedPopulationModel model = generator.getPopulationModelDefinition().getModel();
        Rule ruleA = model.getRules().get(0);
        assertTrue(ruleA instanceof UnicastRule);
    }

    @Test
    public void testParsingRule() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k:boolean};
                
                rule RuleA {
                    A[v > 0] -[v + 2]-> B[v=v, k=(v>0)] |> B[!k] -[((v > 2) || (v==0)): k ? v*2 : v]-> A[v=sender.v > 0 ? sender.v : v]
                }
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertTrue(generator.validate());

        DataOrientedPopulationModel model = generator.getPopulationModelDefinition().getModel();
        Rule ruleA = model.getRules().get(0);
        OutputTransition outputTransition = ruleA.getOutput();
        InputTransition inputTransition = ruleA.getInputs().get(0);

        // A[v > 0]
        assertFalse(outputTransition.predicate().test(0, new ExpressionContext(
                Map.of("v", new SibillaInteger(0)),
                null
        )));
        assertFalse(outputTransition.predicate().test(1, new ExpressionContext(
                Map.of("v", new SibillaInteger(1)),
                null
        )));
        assertTrue(outputTransition.predicate().test(0, new ExpressionContext(
                Map.of("v", new SibillaInteger(1)),
                null
        )));

        //-[v + 2]->

        assertTrue(outputTransition.rate().apply(new ExpressionContext(
                                Map.of(
                                        "v", new SibillaInteger(3)
                                ),
                                null
                        )
                ) == 5
        );

        // B[v=0, k=(v>0)]
        Agent post = outputTransition.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("v", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));

        assertTrue(post.species() == 1);
        assertTrue(post.values().get("v").intOf() == 0);
        assertTrue(!post.values().get("k").booleanOf());

        Agent post1 = outputTransition.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("v", new SibillaInteger(1)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));

        assertTrue(post.species() == 1);
        assertTrue(post1.values().get("v").intOf() == 1);
        assertTrue(post1.values().get("k").booleanOf());

        //B[!k]

        assertTrue(inputTransition.predicate().test(1, new ExpressionContext(Map.of("k", SibillaBoolean.FALSE), null)));
        assertFalse(inputTransition.predicate().test(1, new ExpressionContext(Map.of("k", SibillaBoolean.TRUE), null)));

        // -[((v > 2) || (v==0)): k ? v*2 : v]->

        assertTrue(inputTransition.senderPredicate().test(new ExpressionContext(Map.of("v", new SibillaInteger(3)), null)));
        assertTrue(inputTransition.senderPredicate().test(new ExpressionContext(Map.of("v", new SibillaInteger(0)), null)));
        assertFalse(inputTransition.senderPredicate().test(new ExpressionContext(Map.of("v", new SibillaInteger(2)), null)));

        assertTrue(inputTransition.probability().apply(new ExpressionContext(
                                Map.of(
                                        "v", new SibillaInteger(3),
                                        "k", SibillaBoolean.TRUE
                                ),
                                null
                        )
                ) == 6
        );

        assertTrue(inputTransition.probability().apply(new ExpressionContext(
                                Map.of(
                                        "v", new SibillaInteger(3),
                                        "k", SibillaBoolean.FALSE
                                ),
                                null
                        )
                ) == 3
        );

        //A[v=sender.v > 0 ? sender.v : v]

        Agent post2 = inputTransition.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("v", new SibillaInteger(1)),
                        Map.of("v", new SibillaInteger(0)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));

        assertTrue(post2.species() == 0);
        assertTrue(post2.values().get("v").intOf() == 1);

        Agent post3 = inputTransition.post().sampleDeltas(
                new ExpressionContext(
                        Map.of("v", new SibillaInteger(1)),
                        Map.of("v", new SibillaInteger(2)),
                        null
                ),
                1,
                new SplittableRandomGenerator()
        ).agentDeltaStream().findFirst().map(AgentDelta::agent).orElse(new Agent(0, Collections.emptyMap()));

        assertTrue(post3.species() == 0);
        assertTrue(post3.values().get("v").intOf() == 2);
    }

    @Test
    public void testParsingSystem() throws ModelGenerationException {
        String code = """
                species A {v: integer};
                species B {v: integer, k: boolean};
                
                system s = A[v=0]#100 | B[v=0,k=true];
                """;
        DataOrientedPopulationModelGenerator generator = new DataOrientedPopulationModelGenerator(code);
        assertTrue(generator.validate());
        AgentState s = generator.getPopulationModelDefinition().getConfiguration("s").apply(new SplittableRandomGenerator());

        assertTrue(s.getAgents().get(new Agent(0, Map.of("v", new SibillaInteger(0)))) == 100);
        assertTrue(s.getAgents().get(new Agent(1, Map.of("v", new SibillaInteger(0), "k", SibillaBoolean.TRUE))) == 1);

    }
}
