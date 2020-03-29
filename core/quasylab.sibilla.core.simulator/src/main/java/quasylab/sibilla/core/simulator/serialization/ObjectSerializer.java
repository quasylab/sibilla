package quasylab.sibilla.core.simulator.serialization;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

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
