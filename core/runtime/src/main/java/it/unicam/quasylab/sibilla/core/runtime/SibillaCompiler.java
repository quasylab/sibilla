/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.runtime;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SibillaCompiler {

    private final File workingDirectory;
    private final ClassLoader classLoader;

    public SibillaCompiler() throws IOException {
        this(Files.createTempDirectory(UUID.randomUUID().toString()));
    }

    public SibillaCompiler(String workingDirectory) throws IOException {
        this(Paths.get(workingDirectory));
    }

    public SibillaCompiler(Path workindDirectory) throws IOException {
        this(workindDirectory, true);
    }

    public SibillaCompiler(Path path, boolean create) throws IOException {
        this.workingDirectory = new File(path.toUri());
        if (!workingDirectory.exists()) {
            if (create) {
                this.workingDirectory.mkdir();
            } else {
                throw new IllegalArgumentException("Working directory " + path + " does not exits!");
            }
        }
        this.classLoader = URLClassLoader.newInstance(new URL[]{this.workingDirectory.toURI().toURL()});
    }


    public void compile(String packageName, String className, String source) throws IOException {
        String fileDir = packageName.replace(".", File.separator);

        Path tmp = Paths.get(workingDirectory.getAbsolutePath(), fileDir);
        File sourceDir = new File(tmp.toUri()); // On Windows running on C:\, this is C:\java.
        sourceDir.mkdirs();
        File sourceFile = new File(sourceDir, className + ".java");
        Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int success = compiler.run(null, null, null, sourceFile.getPath());
        if(success!=0){
            throw new IOException("Failed compilation");
        }
    }

    public <T> T getIstance(String packageName, String className, String source, Class<T> clazz) throws IOException, ReflectiveOperationException {
        compile(packageName, className, source);
        Class<?> cls = Class.forName(packageName + "." + className, true, classLoader); // Should print "hello".
        return clazz.cast(cls.getDeclaredConstructor().newInstance()); // Should print "world"
    }

    /*
    public MoonLightScript getIstance(String packageName, String className, String source) throws IOException, ReflectiveOperationException {
        return getIstance(packageName, className, source, MoonLightScript.class);
    }

    public MoonLightTemporalScript loadTemporalScript(String packageName, String className, String source) throws IOException, ReflectiveOperationException {
        return getIstance(packageName, className, source, MoonLightTemporalScript.class);
    }

    public MoonLightSpatialTemporalScript loadSpatialTemporalScript(String packageName, String className, String source) throws IOException, ReflectiveOperationException {
        return getIstance(packageName, className, source, MoonLightSpatialTemporalScript.class);
    }
    */


}
