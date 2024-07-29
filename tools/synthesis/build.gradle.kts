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
    id("it.unicam.quasylab.sibilla.api-lang-conventions")
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("com.google.guava:guava:32.1.0-jre") // used to resolve CVE-2023-2976 vulnerability override the dependency by specifying the version to be greater than 32.0.1.
    implementation("tech.tablesaw:tablesaw-core:0.43.1")
    implementation("org.slf4j:slf4j-nop:2.0.7")
    implementation("com.github.haifengl:smile-core:3.1.0")

}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}