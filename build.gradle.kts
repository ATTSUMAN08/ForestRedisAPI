plugins {
    id("java")
    id("maven-publish")
    alias(libs.plugins.shadow)
}

group = "cz.foresttech"
version = "1.3.1"

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

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("redis.clients", "cz.foresttech.forestredis.shade.jedis")
    relocate("com.google.gson", "cz.foresttech.forestredis.shade.gson")
    relocate("org.yaml.snakeyaml", "cz.foresttech.forestredis.shade.snakeyaml")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
        expand(props)
    }
}
