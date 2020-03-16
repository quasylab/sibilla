package quasylab.sibilla.core.simulator.serialization;

public class CustomClassLoader extends ClassLoader{
    public Class<?> defClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}