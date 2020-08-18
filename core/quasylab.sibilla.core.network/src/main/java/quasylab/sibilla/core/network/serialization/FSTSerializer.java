package quasylab.sibilla.core.network.serialization;

import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

public class FSTSerializer implements Serializer {

    /**
     * This class defines the encoders/decoders used during FST serialization.
     * Usually you just create one global singleton (instantiation of this class is very expensive).
     */
    private FSTConfiguration conf;

    public FSTSerializer() {
        this.conf = FSTConfiguration.createDefaultConfiguration();
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
