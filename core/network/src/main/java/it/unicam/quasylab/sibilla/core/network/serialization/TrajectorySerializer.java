/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.network.serialization;

import it.unicam.quasylab.sibilla.core.models.Model;
import it.unicam.quasylab.sibilla.core.models.State;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Sample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Utility class that handles serialization and deserialization of Trajectories.
 * Data is serialized and deserialized directly into byte arrays to reduce time.
 *
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */
public class TrajectorySerializer {

    /**
     * Serialize a Trajectory into an array of bytes.
     *
     * @param t     the trajectory to serialize
     * @param model the model of the simulation
     * @param <S>   the state class
     * @return byte array of serialized trajectory
     * @throws IOException
     */
    public static <S extends State> byte[] serialize(Trajectory<S> t, Model<S> model) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(baos, t, model);
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    /**
     * Serialize a Trajectory into an array of bytes.
     *
     * @param toSerializeInto the output stream where the serialized data will be
     *                        put
     * @param t               the trajectory to serialize
     * @param model           the model of the simulation
     * @param <S>             the state class
     * @throws IOException
     */
    public static <S extends State> void serialize(ByteArrayOutputStream toSerializeInto, Trajectory<S> t,
            Model<S> model) throws IOException {
        int numberOfSamples = t.size();
        double start = t.getStart();
        double end = t.getEnd();
        long generationTime = t.getGenerationTime();
        int isSuccessful = t.isSuccessful() ? 1 : 0;
        toSerializeInto.write(ByteBuffer.allocate(4).putInt(numberOfSamples).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(start).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putDouble(end).array());
        toSerializeInto.write(ByteBuffer.allocate(4).putInt(isSuccessful).array());
        toSerializeInto.write(ByteBuffer.allocate(8).putLong(generationTime).array());
        for (Sample<S> sample : t.getData()) {
            SampleSerializer.serialize(toSerializeInto, sample, model);
        }
    }

    /**
     * Deserialize a byte array into a Trajectory
     *
     * @param toDeserialize the byte array that contains serialized data
     * @param model         the model of the simulation
     * @param <S>           the state class
     * @return the deserialized trajectory
     * @throws IOException
     */
    public static <S extends State> Trajectory<S> deserialize(byte[] toDeserialize, Model<S> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        Trajectory<S> t = deserialize(bais, model);
        bais.close();
        return t;
    }

    /**
     * Deserialize data from an InputStream into a Trajectory
     *
     * @param toDeserializeFrom the input stream that contains serialized data
     * @param model             the model of the simulation
     * @param <S>               the state class
     * @return the deserialized trajectory
     * @throws IOException
     */
    public static <S extends State> Trajectory<S> deserialize(ByteArrayInputStream toDeserializeFrom, Model<S> model)
            throws IOException {
        Trajectory<S> t = new Trajectory<S>();

        int numberOfSamples = ByteBuffer.wrap(toDeserializeFrom.readNBytes(4)).getInt();
        double start = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        double end = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getDouble();
        boolean isSuccessful = ByteBuffer.wrap(toDeserializeFrom.readNBytes(4)).getInt() != 0;
        long generationTime = ByteBuffer.wrap(toDeserializeFrom.readNBytes(8)).getLong();
        t.setStart(start);
        t.setEnd(end);
        t.setGenerationTime(generationTime);
        t.setSuccessful(isSuccessful);

        for (int i = 0; i < numberOfSamples; i++) {
            Sample<S> newSample = SampleSerializer.deserialize(toDeserializeFrom, model);
            t.addSample(newSample);
        }
        return t;
    }

}
