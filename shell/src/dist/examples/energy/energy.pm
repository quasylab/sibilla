/*
 * MODEL BY GIACOMO ROCCHETTI
 */

const N = 3; /* User maximum consumption */
const M = 6; /* Producer maximum capacity */

species U of [0,N];
species R of [0,N];
species F of [0,N];
species P of [0,M];

param gainRate = 0.75;
param releaseRate = 2.0;
param failRequestRate = 0.25;
param requestRate = 0.5;

rule user_requests_service for i in [0,N-1]{
    U[i] -[ #U[i]*requestRate ]-> R[i]
}

rule user_gains_service for i in [0,N-1] and j in [1,M]{
    R[i]|P[j] -[ #R[i]*gainRate*%P[j] ]-> U[i+1]|P[j-1]
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

measure n_fail = #F[i for i in [0,N]];
measure n_req = #R[i for i in [0,N]];
measure frac_fail = %F[i for i in [0,N]];
param nprod = 5;

system balanced = U[0]<2*nprod>|P[5]<nprod>;
system unbalanced = U[0]<4*nprod>|P[5]<nprod>;
system cs = U[0]<3*nprod>|P[5]<nprod>;

