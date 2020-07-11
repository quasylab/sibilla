/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.serialization;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * Utility class used to serialize and deserialize data.
 * The class operations are based upon the Apache's SerializationUtils suite.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class ApacheSerializer implements Serializer {

    /**
     * Serializes a Serializable instance.
     *
     * @param toSerialize instance to be compressed
     * @return serialized byte array
     */
    public byte[] serialize(Serializable toSerialize) {
        return SerializationUtils.serialize(toSerialize);
    }

    /**
     * Deserializes a byte array.
     *
     * @param toDeserialize byte array to be deserialized
     * @return deserialized Serializable instance
     */
    public Serializable deserialize(byte[] toDeserialize) {
        return SerializationUtils.deserialize(toDeserialize);
    }

    @Override
    public SerializerType getType() {
        return SerializerType.APACHE;
    }
}
