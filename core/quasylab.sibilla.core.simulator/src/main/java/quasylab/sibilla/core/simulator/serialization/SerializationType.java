package quasylab.sibilla.core.simulator.serialization;

public enum SerializationType{
    FST, DEFAULT;

    public static SerializationType getType(Serializer serializer){
        if(serializer instanceof FSTSerializer)
            return FST;
        if(serializer instanceof DefaultSerializer)
            return DEFAULT;
        else return DEFAULT;
    }
};