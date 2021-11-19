package it.unicam.quasylab.sibilla.langs.yoda;


public enum DataType {

    INTEGER,
    REAL,
    BOOLEAN,
    CHAR,
    STRING,
    NONE;

    /**
     * Check if the DataType is a number or not
     *
     * @return true if it is a number
     */
    public boolean isANumber(){return (this==NONE)||(this==INTEGER)||(this==REAL);}

    /**
     * Check if the input DataType is a subtype of the one compared
     *
     * @param other the other type to be compared
     * @return true if the other is a subtype of this
     */
    public boolean isSubtypeOf (DataType other){
        return (this==NONE)||(other==NONE)||(this==other)||((this==REAL)&&(other==INTEGER))||((this==STRING)&&(other==CHAR));
    }

    /**
     * Merge two DataType inputs
     *
     * @param t1 the first data type
     * @param t2 the second data type
     * @return one of the two data type after being merged
     */
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
