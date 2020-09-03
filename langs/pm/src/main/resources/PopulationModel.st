group PopulationModel;

model(packageName,className,consts,params,ruleDeclarations,rulesToModel,speciesDeclaration,measuresDeclarations) ::= <<
/**
Generated with Sibilla.
*/

package <package>;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.*;

public class <className> extends PopulationModelDefinition {

    <consts; separator="\n">

    public <className>() {
        <params; separator ="\n">
    }

    <ruleDeclarations; separator ="\n\n">

    @Override
    public Model<PopulationState> createModel() {
        PopulationModel model = new PopulationModel(this);
        <rulesToModel; separator = "\n">;
        return model;
    }
}
>>


const(name,value) ::= "public static double <name> = <value>;"

ruleDeclarationMethod(name,body) ::= <<
    public void rule<name>(Model model) {
        <body>
    }
>>


ruleForStatement(name,min,max,body) ::= <<
    for( int <name> = <min> ; <name> \< <max> ; <name>++ ) {
        <body>
    }
>>


ruleWhenStatement(guard,body) ::= <<
    if (<guard>) {
        <body>
    }
>>

ruleBody(name,guard,pre,post,rate) ::= <<
    model.addRule(
        <name> ,
        <guard> ,
        <pre> ,
        <post> ,
        <rate>
    )
>>

systemGeneration(name,args,assignments) ::= <<
    public PopulationState getSystem_<name>(<args; separator = ",") {
        int[] state = new int[getArity()];
        <assignments>
        return new PopulationState(state);
    }
>>

systemParameterDeclaration(name) ::= "int <name>"

speciesAssignment(index,value) ::= "state[index] += value;"

populationPattern(elements) ::= "new Population[] { <elements;separator=\",\"> }"

populationPatterElement(name,args,size) ::= "new Population(<indexOf(name,args)>,<size>)"

rateExpression(value) ::= "<stateName()>"


stateName() ::= "state"

nowValue() ::= "now"

unaryExpression(op,arg) ::= "(<op> <arg>)"

binaryExpression(left,op,right) ::= "(<left> <op> <right>)"

powExpression(base,exponent) ::= "Math.pow(<base>, <exponent>)"

safeDivExpression(left,right) ::= "PopulationModelDefinition.fraction(<left>, <right>)"

reference(name) ::= "__<name>__"

literal(value) ::= "<value>"

ifThenElseExpression(guard,thenBranch,elseBranch) ::= "(<guard>?<thenBranch>:<elseBranch>)"

speciesExpression(name,args) ::= "_SPECIES_<name>_<args:{v|_<v>}>"

populationFractionExpression(name,args) ::= "<stateName()>.getFraction(<speciesExpression(name,args)>)"

populationOccupancyExpression(name,args) ::= "<stateName()>.getOccupancy(<speciesExpression(name,args)>)"