    param lambdaMeet = 1.0;      /* Meeting rate */
    param probInfection = 0.25;  /* Probability of Infection */
    param incubationRate = 1.0;  /* Incubation rate */
    param recoverRate = 0.05;    /* Recovering rate */

    const startS = 90;           /* Initial number of S agents */
    const startI = 10;           /* Initial number of I agents */

    species S;
    species I;
    species R;

    rule infection {
        S|I -[ #S*%I*lambdaMeet*probInfection ]-> E|I
    }

    rule incubation {
        E -[ #E*incubationRate ]-> I
    }

    rule recovered {
        I -[ #I*recoverRate ]-> R
    }

    system init(scale) = S<startS*scale>|I<startI*scale>;