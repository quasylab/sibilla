package it.unicam.quasylab.sibilla.langs.yoda;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;

public enum DataType {

    INTEGER,
    REAL,
    BOOLEAN,
    CHAR,
    STRING,
    NONE;

    public boolean isANumber(){return (this==NONE)||(this==INTEGER)||(this==REAL);}

    public boolean isSubtypeOf (DataType other){
        return (this==NONE)||(other==NONE)||(this==other)||((this==REAL)&&(other==INTEGER))||((this==STRING)&&(other==CHAR));
    }

    public static DataType merge(DataType t1, DataType t2) {
        if (t1==t2) {
            return t2;
        }
        if (t1.isSubtypeOf(t2)) {
            return t1;
        }
        if (t2.isSubtypeOf(t1)) {
            return t2;
        }
        return null;
    }

    @Override
    public String toString(){
        switch (this){
            case INTEGER: return "int";
            case REAL: return  "real";
            case BOOLEAN: return "bool";
            case CHAR: return  "char";
            case STRING: return  "string";
        }
        return "none";
    }



}
