import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	iosX64()
	iosArm64()
	iosSimulatorArm64()

	jvm()

	sourceSets {
		commonMain.dependencies {
			api(libs.kotlinx.datetime)
			api(libs.ktor.client.core)
			api(libs.ktor.client.content)
			api(libs.ktor.serialization.json)
			api(libs.ktor.serialization.protobuf)
			api(libs.ktor.client.logging)
			api(libs.ktor.client.auth)
			api(libs.kotlinx.coroutines.core)
		}
		androidMain.dependencies {
			implementation(libs.ktor.client.okhttp)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}

android {
	namespace = "dev.bitvictory.aeon.shared"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}
}
