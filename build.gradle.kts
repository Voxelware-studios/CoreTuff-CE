plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.extendedclip.com/releases/")
    }
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.bstats.org/repository/maven-public/")
        content {
            includeGroup("org.bstats")
        }
    }

}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly("com.h2database:h2:2.4.240")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    implementation("org.bstats:bstats-bukkit:3.1.0")

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "org.voxelware.coretuff.libs.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G")
        pluginJars(shadowJar.flatMap { it.archiveFile })
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
