package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

public class LTMADS {

    private int a = 0;

    private record myRec1(int i,int j){}
    private record myRec2(int i,int j){}

    private class myInnerClass{
        int b = a;

        public void modifyA(){
            a++;
        }
    }


    public void method(){
        myInnerClass mi = new myInnerClass();
        mi.modifyA();
    }

    public int getA() {
        return a;
    }
}
