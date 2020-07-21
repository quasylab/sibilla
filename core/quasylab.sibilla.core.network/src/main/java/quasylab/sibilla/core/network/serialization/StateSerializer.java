package quasylab.sibilla.core.network.serialization;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.past.State;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class StateSerializer {

    //POPULATIONSTATE
    //population - 8
    //dimensione vettore popolazione - 4
    //valori vettore popolazione
    public static byte[] serialize(State state) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (state instanceof PopulationState) {
            PopulationState popState = (PopulationState) state;
            double population = popState.population();
            int populationVectorLength = popState.size();
            // System.out.println(String.format("Population:%f - Population vector length:%d", population, populationVectorLength));
            baos.write(ByteBuffer.allocate(8).putDouble(population).array());
            baos.write(ByteBuffer.allocate(4).putInt(populationVectorLength).array());
            for (int vectorValue : popState.getPopulationVector()) {
                baos.write(ByteBuffer.allocate(4).putInt(vectorValue).array());
            }
        } else {
            baos.close();
            throw new IOException("State class error");
        }
        byte[] toReturn = baos.toByteArray();
        //System.out.println(String.format("state To send:%d", toReturn.length));
        baos.close();
        return toReturn;
    }

    public static State deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        // System.out.println(String.format("state To deserialize:%d", toDeserialize.length));
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        if (model instanceof PopulationModel) {
            double population = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
            int populationVectorLength = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            //  System.out.println(String.format("Population:%f - Population vector length:%d", population, populationVectorLength));
            int[] populationVector = new int[populationVectorLength];
            for (int i = 0; i < populationVectorLength; i++) {
                populationVector[i] = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
            }
            bais.close();
            return new PopulationState(population, populationVector);
        } else {
            bais.close();
            throw new IOException("State class error");
        }
    }

}
