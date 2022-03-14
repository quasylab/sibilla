package it.unicam.quasylab.sibilla.core.models.yoda;

public class YodaLambda {
    private final String variable;
    private final YodaValue value;

    public YodaLambda(String variable, YodaValue value){
        this.variable = variable;
        this.value = value;
    }

    public String getVariable(){
        return variable;
    }

    public YodaValue getValue() {return value;}

}
