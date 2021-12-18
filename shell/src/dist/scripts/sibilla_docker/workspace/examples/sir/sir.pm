    param lambdaMeet = 1.0;      /* Meeting rate */
    param probInfection = 0.25;  /* Probability of Infection */
    param recoverRate = 0.05;    /* Recovering rate */

    const startS = 90;           /* Initial number of S agents */
    const startI = 10;           /* Initial number of I agents */

    species S;
    species I;
    species R;

    rule infection {
        S|I -[ 1.0 ]-> I|I
    }

    rule recovered {
        I -[ 1.0 ]-> R
    }

    system init = S<startS>|I<startI>;

    predicate allRecovered = (#S+#I==0);