species S;
species E;
species I;
species R;

rule s_to_e {
    S|I -[ 1.0*%I ]-> E|I
}

rule e_to_i {
    E -[ 1.0 ]-> I
}

rule i_to_r {
    I -[ 1.0 ]-> R
}

system init = S<10>|I;