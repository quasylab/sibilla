plugins {
    id("it.unicam.quasylab.sibilla.api-lang-conventions")
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation(project(":core:simulator"))
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-long-messages")
}


