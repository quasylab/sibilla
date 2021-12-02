package it.unicam.quasylab.sibilla.core.models.yoda;

import it.unicam.quasylab.sibilla.core.models.AbstractModel;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;

import java.util.Map;

public abstract class YodaModel extends AbstractModel<MappingState> {
    public YodaModel(Map<String, Measure<MappingState>> measuresTable) {
        super(measuresTable);
    }
}
