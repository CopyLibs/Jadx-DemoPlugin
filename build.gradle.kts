import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm") version "2.1.20"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
	jvmToolchain(17)
}

group = "io.github.copylibs"
version = "1.0.1"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("io.github.skylot:jadx-core:1.5.5")
}

tasks {
	val shadowJar = withType(ShadowJar::class) {
		archiveClassifier.set("")
	}

	register<Copy>("dist") {
		group = "jadx-plugin"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("dist"))
	}
}
