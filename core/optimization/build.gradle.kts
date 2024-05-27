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



plugins {
    id("it.unicam.quasylab.sibilla.java-library-conventions")
}

dependencies {


    // Tablesaw
    implementation("tech.tablesaw:tablesaw-core:0.43.1")

    // 3.0.3 implementation

    implementation("com.github.haifengl:smile-core:3.1.0")

    // slf4j-jdk14 dependency
    implementation("org.slf4j:slf4j-nop:2.0.7")

//    implementation("org.bytedeco:javacpp-platform:1.5.3")
//    implementation("org.bytedeco:openblas-platform:0.3.9-1.5.3")
//    implementation("org.bytedeco:arpack-ng-platform:3.7.0-1.5.3")




    //implementation("com.github.haifengl:smile-core:2.6.0")

    //implementation("org.bytedeco:javacv:1.5.9")
    //implementation("com.github.haifengl:smile-mkl:2.6.0")


    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    // https://mvnrepository.com/artifact/org.ejml/dense64
    implementation("org.ejml:dense64:0.30")

    implementation(project(":core:simulator"))
}
