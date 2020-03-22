package quasylab.sibilla.core.simulator.serialization;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public class ObjectSerializer {

	public static byte[] serializeObject(Serializable toSerialize) {
		byte[] data = SerializationUtils.serialize(toSerialize);
		return data;
	}
	
	public static Serializable deserializeObject(byte[] toDeserialize) {
		Serializable object = SerializationUtils.deserialize(toDeserialize);
		return object;
	}
}
