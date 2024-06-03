package it.unicam.quasylab.sibilla.core.runtime.command;

import it.unicam.quasylab.sibilla.core.runtime.OptimizationModule;
import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;

public class InitializationCommand extends AbstractSibillaCommand {
    public InitializationCommand(RuntimeContext runtimeContext) {
        super(runtimeContext);
    }

    @Override
    public void execute() {
        for (SibillaModule m: SibillaModule.MODULES) {
            rc.getModuleIndex().put(m.getModuleName(),m);
            if (rc.getCurrentModule() == null) {
                rc.setCurrentModule(m);
            }
        }
        rc.setOptimizationModule(new OptimizationModule());
    }
}
