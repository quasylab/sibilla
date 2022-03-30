species A;
species B;

const lambda = 1.0;
const NA = 50;
const NB = 50;

rule a_to_b {
    A -[ #A*lambda*(1-%B) ]-> B
}

rule b_to_a {
    B -[ #B*lambda*(1-%A) ]-> A
}

system balanced = A<NA>|B<NB>;
