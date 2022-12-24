const startW = 20;  /* Initial number of wolves */
const startS = 100; /* Initial number of sheeps */
const N = 10;       /* Width of the Grid */
const M = 10;       /* Length of the Grid */

param lambdaMovementS = 5; /* Rate of sheeps movement */
param lambdaMovementW = 2; /* Rate of wolves movement */
param lambdaMeet = 10.0;     /* Rate of wolves meeting with sheeps */
param eatingProb = 1 ;     /* Probability of wolf eating a sheep */

species W of [0,N]*[0,M];
species S of [0,N]*[0,M];

rule go_up_S for i in [0,N] and j in [0,M] when j<M-1{
    S[i,j] -[ lambdaMovementS * (1 - (%W[i,j+1])) ]-> S[i,j+1]
}

rule go_down_S for i in [0,N] and j in [0,M] when j>0{
    S[i,j] -[ lambdaMovementS * (1 - (%W[i,j-1])) ]-> S[i,j-1]
}

rule go_right_S for i in [0,N] and j in [0,M] when i<N-1{
    S[i,j] -[ lambdaMovementS * (1 - (%W[i+1,j])) ]-> S[i+1,j]
}

rule go_left_S for i in [0,N] and j in [0,M] when i>0{
    S[i,j] -[ lambdaMovementS * (1 - (%W[i-1,j])) ]-> S[i-1,j]
}

rule eating for i in [0,N] and j in [0,M]{
    W[i,j]|S[i,j] -[ (lambdaMeet*(%W[i,j])*(eatingProb)) ]-> W[i,j]
}

rule go_up_W for i in [0,N] and j in [0,M] when j<M-1{
    W[i,j] -[ lambdaMovementW * (%S[i,j+1]) ]-> W[i,j+1]
}

rule go_down_W for i in [0,N] and j in [0,M] when j>0{
    W[i,j] -[ lambdaMovementW * (%S[i,j-1]) ]-> W[i,j-1]
}

rule go_right_W for i in [0,N] and j in [0,M] when i<N-1{
    W[i,j] -[ lambdaMovementW * (%S[i+1,j]) ]-> W[i+1,j]
}

rule go_left_W for i in [0,N] and j in [0,M] when i>0{
    W[i,j] -[ lambdaMovementW * (%S[i-1,j]) ]-> W[i-1,j]
}

system start = W[5,5]<startW>|S[5,5]<startS>;