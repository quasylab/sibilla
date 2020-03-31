package quasylab.sibilla.core.simulator.serialization;

import java.lang.reflect.Method;

public class CustomClassLoader {

    public static void resClass(Class<?> clazz) {
        try {
            Method m = ClassLoader.class.getDeclaredMethod("resolveClass", Class.class);
            m.setAccessible(true);
            m.invoke(ClassLoader.getSystemClassLoader(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}