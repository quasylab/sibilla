species S0;
species S1;
species SU;

const meetRate = 1.0;

rule su_to_s1 {
    SU|S1 -[ #SU*meetRate*%S1 ]-> S1|S1
}

rule su_to_s0 {
    SU|S0 -[ #SU*meetRate*%S0 ]-> S0|S0
}

rule s1_to_su {
    S1|S0 -[ #S1*meetRate*%S0 ]-> SU|S0
}

rule s0_to_su {
    S0|S1 -[ #S0*meetRate*%S1 ]-> SU|S1
}

param scale = 1.0;

system balanced = S0<10*scale>|S1<10*scale>|SU<10*scale>;


system custom(s0,s1,su) = S0<s0>|S1<s1>|SU<su>;