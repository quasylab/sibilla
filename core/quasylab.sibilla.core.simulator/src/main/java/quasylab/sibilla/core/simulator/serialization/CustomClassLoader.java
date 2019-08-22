package quasylab.sibilla.core.simulator.serialization;

public class CustomClassLoader extends ClassLoader{
    public Class<?> defClass(String name, byte[] byteCode) {
        return defineClass(name, byteCode, 0, byteCode.length);
    }
}