package it.unicam.quasylab.sibilla.core.optimization.sampling;

public class Main {
    public static void main(String[] args) {
        DiscreteStepInterval d = new DiscreteStepInterval(0,10,1);
        myMethod(d);
    }

    public static void myMethod(Interval i){
        System.out.println(i);
        System.out.println(i.isContinuous());
    }
}
