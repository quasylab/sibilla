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
    implementation("org.apache.commons:commons-math3:3.0")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("org.apache.commons:commons-io:1.3.2")
    implementation("de.ruedigermoeller:fst:3.0.4-jdk17")
    api(project(":core:simulator"))
}
