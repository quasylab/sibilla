const N = 10;
const M = 10;

species A of [0,N]*[0,M];
species B of [0,N]*[0,M];

param movementRate = 1.0;

rule go_up_A for i in [0,N] and j in [0,M] when i<N-1 {
    A[i,j] -[ movementRate*(1-(%A[i+1,j]+%B[i+1,j])) ]-> A[i+1,j]
}

rule go_down_A for i in [0,N] and j in [0,M] when 0<i {
    A[i,j] -[ movementRate*(1-(%A[i-1,j]+%B[i-1,j])) ]-> A[i-1,j]
}

rule go_left_A for i in [0,N] and j in [0,M] when 0<j {
    A[i,j] -[ movementRate*(1-(%A[i,j-1]+%B[i,j-1])) ]-> A[i,j-1]
}

rule go_right_A for i in [0,N] and j in [0,M] when j<M-1 {
    A[i,j] -[ movementRate*(1-(%A[i,j+1]+%B[i,j+1])) ]-> B[i,j+1]
}

rule go_up_B for i in [0,N] and j in [0,M] when i<N-1 {
    B[i,j] -[ movementRate*(%A[i+1,j]+%B[i+1,j]) ]-> B[i+1,j]
}

rule go_down_B for i in [0,N] and j in [0,M] when 0<i {
    B[i,j] -[ movementRate*(%A[i-1,j]+%B[i-1,j]) ]-> B[i-1,j]
}

rule go_left_B for i in [0,N] and j in [0,M] when 0<j {
    B[i,j] -[ movementRate*(%A[i,j-1]+%B[i,j-1]) ]-> B[i,j-1]
}

rule go_right_B for i in [0,N] and j in [0,M] when j<M-1 {
    B[i,j] -[ movementRate*(%A[i,j+1]+%B[i,j+1]) ]-> B[i,j+1]
}

system start = A[0,0]<50>|B[0,0]<50>;

