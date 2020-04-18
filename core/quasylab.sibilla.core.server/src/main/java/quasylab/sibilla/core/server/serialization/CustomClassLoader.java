/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server.serialization;

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