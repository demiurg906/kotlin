
plugins {
    kotlin("jvm")
    id("jps-compatible")
}

jvmTarget = "1.6"

dependencies {
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:resolution"))
    compile(project(":compiler:plugin-api"))
    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }
    compileOnly(intellijDep()) { includeJars("annotations", "trove4j", "guava", rootProject = rootProject) }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
