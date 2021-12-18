species S0;
species S1;
species SU;

param scale = 1.0;

const changeRate = 1.0;

rule su_to_s1 {
    SU -[ #SU*changeRate*0.5 ]-> S1
}

rule su_to_s0 {
    SU -[ #SU*changeRate*0.5 ]-> S0
}


system stateA = SU<10*scale>;


predicate completed = (%SU==0.0);