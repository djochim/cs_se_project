import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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

	@OptIn(ExperimentalWasmDsl::class)
	wasmJs {
		browser {
			val rootDirPath = project.rootDir.path
			val projectDirPath = project.projectDir.path
			commonWebpackConfig {
				devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
					static = (static ?: mutableListOf()).apply {
						// Serve sources to debug inside browser
						add(rootDirPath)
						add(projectDirPath)
					}
				}
			}
		}
	}

	sourceSets {
		commonMain.dependencies {
			api(libs.kotlinx.datetime)
			api(libs.ktor.client.core)
			api(libs.ktor.client.content)
			api(libs.ktor.serialization.json)
			api(libs.ktor.serialization.protobuf)
			api(libs.ktor.client.logging)
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
