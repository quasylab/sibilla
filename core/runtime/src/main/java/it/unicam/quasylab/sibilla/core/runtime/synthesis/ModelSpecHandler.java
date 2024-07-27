package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.core.runtime.SibillaModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class ModelSpecHandler {

    private final Map<String, Object> modelSpec;

    public ModelSpecHandler(Map<String, Object> modelSpec){
        this.modelSpec = modelSpec;
    }

    private  final String UNKNOWN_MODULE_MESSAGE = "%s is unknown! Available modules are: " +
            Arrays.stream(availableModules()).reduce("\n",(a, b)->a +"\n"+b);


    private  String[] availableModules(){
        return SibillaModule.MODULES.stream()
                .map(SibillaModule::getModuleName)
                .toList().toArray(new String[0]);
    }

    private boolean isModuleAvailable(String moduleName){
        return Arrays.asList(availableModules()).contains(moduleName);
    }

    public  ModelSpecs getModelSpecs() {
        String module = (String) modelSpec.get("module");
        String initialConfiguration = (String) modelSpec.get("initialConfiguration");
        String modelSpecification;
        String modelSpecificationValue = (String) modelSpec.get("modelSpecification");
        String modelSpecificationPath = (String) modelSpec.get("modelSpecificationPath");

        if (modelSpecificationValue == null && modelSpecificationPath == null) {
            throw new IllegalArgumentException("Both modelSpecification and modelSpecificationPath are null");
        } else if (modelSpecificationValue != null) {
            modelSpecification = modelSpecificationValue;
        } else {
            try {
                modelSpecification = Files.readString(Path.of(modelSpecificationPath));
            } catch (IOException e) {
                throw new RuntimeException("Error reading modelSpecificationPath", e);
            }
        }
        if (module == null || initialConfiguration == null || modelSpecification == null)
            throw new IllegalArgumentException("Model must contain module, initialConfiguration, and modelSpecification!");
        if(!isModuleAvailable(module))
            throw new IllegalArgumentException(String.format(UNKNOWN_MODULE_MESSAGE,module));
        return new ModelSpecs(module, initialConfiguration, modelSpecification);
    }
}
