species B;
species A;

const lambda = 1.0;
const NA = 10;
const NB = 10;

rule b_to_a {
    B -[ #B*lambda*%A ]-> A
}

rule a_to_b {
    A -[ #A*lambda*%B ]-> B
}


system balanced = A<NA>|B<NB>;
