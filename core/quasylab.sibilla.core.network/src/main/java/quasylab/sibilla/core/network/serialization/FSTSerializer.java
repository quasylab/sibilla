package quasylab.sibilla.core.network.serialization;

import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

public class FSTSerializer implements Serializer {

    private FSTConfiguration conf;

    public FSTSerializer(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public byte[] serialize(Serializable toSerialize) {
        return conf.asByteArray(toSerialize);
    }

    @Override
    public Serializable deserialize(byte[] toDeserialize) {
        return (Serializable) conf.asObject(toDeserialize);
    }

    @Override
    public SerializerType getType() {
        return SerializerType.FST;
    }
}
