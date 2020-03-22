package quasylab.sibilla.core.simulator.serialization;

import java.lang.reflect.Method;

public class CustomClassLoader extends ClassLoader {
	public Class<?> defClass(String name, byte[] b) {
		try {
			Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class,
					int.class);
			m.setAccessible(true);
			return (Class<?>) m.invoke(ClassLoader.getSystemClassLoader(), name, b, 0, b.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}