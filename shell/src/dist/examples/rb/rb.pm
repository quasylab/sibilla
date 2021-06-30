param N = 10;

species R of [0,N];
species B of [0,N];

rule r_to_b [ for i in [0,N-1]] {
    R[i] -[ 1.0 ]-> R[i+1]
}

