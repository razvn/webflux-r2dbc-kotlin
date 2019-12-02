import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.1.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	id("name.remal.check-dependency-updates") version "1.0.158"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
}

group = "net.razvan.poc.spring-boot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

// versions


val reactorKotlinExtensionsVersion = "1.0.1.RELEASE"
val h2Version = "1.4.200" // with 200 there are errors as data-r2dbc is not yet updated
val mockkVersion = "1.9.3"
val  springBootBomR2DbcVersion = "0.1.0.M2"
val  jacksonKotlinModuleVersion = "2.10.1"
val  r2dbch2Version = "0.8.0.RELEASE"
val  reactorTestVersion = "3.3.1.RELEASE"

dependencies {
	implementation("org.springframework.boot.experimental:spring-boot-actuator-autoconfigure-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot.experimental:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinModuleVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactorKotlinExtensionsVersion")
	implementation("com.h2database:h2:$h2Version")
	implementation("io.r2dbc:r2dbc-h2:$r2dbch2Version")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.springframework.boot.experimental:spring-boot-test-autoconfigure-r2dbc")
	testImplementation("io.projectreactor:reactor-test:$reactorTestVersion")
	testImplementation("io.mockk:mockk:$mockkVersion")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot.experimental:spring-boot-bom-r2dbc:$springBootBomR2DbcVersion")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
