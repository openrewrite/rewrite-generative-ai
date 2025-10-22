import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension

plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
}

group = "org.openrewrite"
description = "Rewrite recipes with a generative AI twist."

//The bom version can also be set to a specific version or latest.release.
val rewriteVersion = rewriteRecipe.rewriteVersion.get()


dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    implementation(platform("org.openrewrite:rewrite-bom:${rewriteVersion}"))

    implementation("org.openrewrite:rewrite-java")
    runtimeOnly("org.openrewrite:rewrite-java-21")
    // Need to have a slf4j binding to see any output enabled from the parser.
    runtimeOnly("ch.qos.logback:logback-classic:1.5.20")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.+")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.assertj:assertj-core:latest.release")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
