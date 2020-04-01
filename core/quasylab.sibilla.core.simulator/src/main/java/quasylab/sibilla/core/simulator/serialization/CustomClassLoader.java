package quasylab.sibilla.core.simulator.serialization;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CustomClassLoader extends ClassLoader {

	public static Map<String, byte[]> classes = new HashMap<>();

	public static Class<?> defClass(String name, byte[] b) {
		try {
			Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class,
					int.class);
			m.setAccessible(true);
			classes.put(name, b);
			return (Class<?>) m.invoke(ClassLoader.getSystemClassLoader(), name, b, 0, b.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}