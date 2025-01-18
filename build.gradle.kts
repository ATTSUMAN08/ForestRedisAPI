plugins {
    id("java")
    id("maven-publish")
    alias(libs.plugins.shadow)
}

group = "cz.foresttech"
version = "1.3.8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // PaperMC, Velocity
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.velocityApi)
    compileOnly(libs.bungeeApi)

    implementation(libs.jedis)
    implementation(libs.gson)
    implementation(libs.snakeYaml)
}

val targetJavaVersion = 17

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("redis.clients.jedis", "cz.foresttech.forestredis.shade.jedis")
    relocate("com.google.gson", "cz.foresttech.forestredis.shade.gson")
    relocate("org.yaml.snakeyaml", "cz.foresttech.forestredis.shade.snakeyaml")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
        expand(props)
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveClassifier.set("raw")
}

tasks.publishToMavenLocal {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
    repositories {
        mavenLocal()
    }
}
