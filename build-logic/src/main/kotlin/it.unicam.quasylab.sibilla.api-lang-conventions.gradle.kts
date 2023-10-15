
plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("it.unicam.quasylab.sibilla.java-common-conventions")

    // Apply the java-library plugin for API and implementation separation.
    `antlr`
}


dependencies {
    // https://mvnrepository.com/artifact/org.antlr/antlr4
    antlr("org.antlr:antlr4:4.13.1")

    // https://mvnrepository.com/artifact/org.antlr/antlr4-runtime
    implementation("org.antlr:antlr4-runtime:4.13.1")

    implementation(project(":core:simulator"))
    implementation(project(":langs:util"))

}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}