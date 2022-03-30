species A;
species B;

const lambda = 1.0;
const NA = 10;
const NB = 10;

rule a_to_b {
    A -[ #A*lambda*%B ]-> B
}

rule b_to_a {
    B -[ #B*lambda*%A ]-> A
}

system balanced = A<NA>|B<NB>;
