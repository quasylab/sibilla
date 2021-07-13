species GA;
species GB;
species CA;
species CB;

param lambda = 1.0;

label typeA = {GA, CA};
label typeB = {GB, CB}

rule groupieChangeA {
    GB -[ #GB*lambda*%typeA ]-> GA
}

rule groupieChangeB {
    GA -[ #GA*lambda*%typeB ]-> GB
}

rule celebrityChangeA {
    CB -[ #GB*lambda*%typeB ]-> CA
}

rule celebrityChangeB {
    CA -[ #GB*lambda*%typeA ]-> CB
}

system allGroupiesA(N) = A#(N);


