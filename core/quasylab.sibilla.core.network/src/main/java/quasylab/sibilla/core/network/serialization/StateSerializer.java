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
            baos.write(ByteBuffer.allocate(8).putDouble(popState.population()).array());
            baos.write(ByteBuffer.allocate(4).putInt(popState.size()).array());
            for (int vectorValue : popState.getPopulationVector()) {
                baos.write(ByteBuffer.allocate(4).putInt(vectorValue).array());
            }
        } else {
            baos.close();
            throw new IOException("State class error");
        }
        byte[] toReturn = baos.toByteArray();
        baos.close();
        return toReturn;
    }

    public static State deserialize(byte[] toDeserialize, Model<? extends State> model) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(toDeserialize);
        if (model instanceof PopulationModel) {
            double population = ByteBuffer.wrap(bais.readNBytes(8)).getDouble();
            int populationVectorLength = ByteBuffer.wrap(bais.readNBytes(4)).getInt();
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
