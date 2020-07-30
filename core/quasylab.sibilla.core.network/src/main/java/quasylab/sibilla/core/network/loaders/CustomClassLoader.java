/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.network.loaders;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class used to load the data associated to a .class file into the memory.
 *
 * @author Belenchia Matteo
 * @author Stelluti Francesco Pio
 * @author Zamponi Marco
 */

public class CustomClassLoader extends ClassLoader {

    /**
     * Map that contains the names of all the classes loaded into memory through this loader.
     * All the names are associated with the byte array containing the datas of the .class file of the class related to the given name.
     */
    private static Map<String, byte[]> classes = new HashMap<>();

    /**
     * Retrieves the byte array associated to a class name that was previously loaded through this loader.
     *
     * @param className the name of the class which byte array data needs to be retrieved.
     * @return byte array associated with the requested class name.
     */
    public static byte[] loadClassBytes(String className) {
        return classes.get(className);
    }

    /**
     * Deletes the byte array associated to a class name that was previously loaded through this loader.
     *
     * @param className the name of the class which byte array data needs to be deleted.
     * @return byte array associated with the requested class name or null if the class wasn't loaded using this loader.
     */
    public static byte[] removeClassBytes(String className) {
        return classes.remove(className);
    }

    /**
     * Loads into memory the data associated to a .class file
     *
     * @param name of the class to be loaded in memory.
     * @param b    byte array containing the data of the class to be loaded in memory.
     */
    public static void defClass(String name, byte[] b) {
        try {
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class,
                    int.class);
            m.setAccessible(true);
            classes.put(name, b);
            m.invoke(ClassLoader.getSystemClassLoader(), name, b, 0, b.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}