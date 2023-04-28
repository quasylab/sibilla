param meetRate = 1.0;      /* Meeting rate */
param infectionRate = 0.005;  /* Probability of Infection */
param recoverRate = 0.005;    /* Recovering rate */

const startS = 95;           /* Initial number of S agents */
const startI = 5;           /* Initial number of I agents */
const startR = 0;            /* Initial number of R agents */

species S;
species I;
species R;

rule infection {
    S|I -[ #S * %I * meetRate * infectionRate ]-> I|I
}

rule recovered {
    I -[ #I * recoverRate ]-> R
}

system init = S<startS>|I<startI>|R<startR>;
predicate allRecovered = (#S+#I==0);