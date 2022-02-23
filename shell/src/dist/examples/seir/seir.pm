param lambdaMeet = 10.0; /* Meeting rate */
param lambdaExposure = 1.00; /* rate of Exposure */
param lambdaInfection = 2.00; /* rate of Infection */
param lambdaRecovery = 0.5; /* rate of recovery */

const startS = 99; /* Initial number of S agents */
const startI = 1; /* Initial number of I agents */

species S;
species E;
species I;
species R;

rule exposure {
    S|I -[ #S *%I* lambdaMeet * lambdaExposure ]-> E|I
}
rule infection {
    E -[ #E* lambdaInfection ]-> I
}
rule recovered {
    I -[ #I* lambdaRecovery ]-> R
}

system initial_1 = S < startS >|I < startI >;
system initial_2 = S < 98 >|I < 2 >;

predicate allRecovered = (# S +# E +# I ==0) ;
