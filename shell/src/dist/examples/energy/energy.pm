param N = 3; /* User maximum consumption */
param M = 5; /* Producer maximum capacity */

species U of [0,N];
species R of [0,N];
species F of [0,N];
species P of [0,M];
/* species FP; */

const malfunctionRate = 0.5;
const repairRate = 0.5;
const gainRate = 1.0;
const releaseRate = 1.0;
const failRequestRate = 0.5;
const requestRate = 1.0; 

/*
rule producer_malfunction for i in [0,M]{
    P[i] -[ #P[i] * malfunctionRate ]-> FP
}

rule producer_repaired{
    FP -[ #FP * repairRate ]-> P[M]
}
*/

rule user_requests_service for i in [0,N-1]{
    U[i] -[ #U[i]*requestRate ]-> R[i]
}

rule user_gains_service for i in [0,N-1] and j in [1,M]{
    R[i]|P[j] -[ #R[i]*gainRate ]-> U[i+1]|P[j-1]
}

rule user_releases_service for i in [1,N] and j in [0,M-1]{
    U[i]|P[j] -[ #U[i] * releaseRate * %P[j] ]-> U[i-1]|P[j+1]
}

rule failed_request for i in [0,N-1]{
    R[i] -[ #R[i] * failRequestRate * %P[0] ]-> F[i]
}

rule failed_user_gains_service for i in [0,N-1] and j in [1,M]{
    F[i]|P[j] -[ #F[i] * gainRate * %P[j] ]-> U[i+1]|P[j-1]
}


system init = U[0]|P[4];
