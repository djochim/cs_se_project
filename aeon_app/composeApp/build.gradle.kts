import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.kover)
	alias(libs.plugins.mokkery)
	kotlin("plugin.allopen") version libs.versions.kotlin.get()
	kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}

		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	sourceSets {
		androidMain.dependencies {
			implementation(compose.preview)
			implementation(libs.androidx.activity.compose)
			implementation(libs.androidx.crypto)
		}
		commonMain.dependencies {
			implementation(projects.shared)
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)
			implementation(libs.compose.navigation)
			implementation(libs.androidx.lifecycle.viewmodel)
			implementation(libs.androidx.lifecycle.viewmodel.compose)
			implementation(libs.androidx.lifecycle.runtime.compose)
			implementation(libs.markdown.renderer)
			implementation(compose.material3AdaptiveNavigationSuite)
			implementation(compose.materialIconsExtended)
			implementation(libs.multiplatform.settings)
			implementation(libs.multiplatform.settings.serialization)
			implementation(libs.koin.android.core)
			implementation(libs.koin.compose)
			implementation(libs.koin.compose.viewmodel)
			implementation(libs.koin.compose.viewmodel.navigation)
			implementation(libs.kermit)
			implementation(libs.coil)
			implementation(libs.coil.compose)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
			implementation(libs.kotest.assert)
			@OptIn(ExperimentalComposeLibrary::class)
			implementation(compose.uiTest)
//			implementation(libs.mockative )
		}
	}
}

tasks.withType<Test> {
	exclude("**/*ComposeTest*")
}

android {
	namespace = "dev.bitvictory.aeon"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "dev.bitvictory.aeon"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 1
		versionName = "1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
			excludes += "/META-INF/LICENSE.md"
			excludes += "/META-INF/LICENSE-notice.md"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

kover {
	reports {
		total {
			verify {
				rule("Minimal line coverage in percent") {
					minBound(30, aggregationForGroup = AggregationType.COVERED_PERCENTAGE, coverageUnits = CoverageUnit.LINE)
				}
				rule("Minimal banch coverage in percent") {
					minBound(50, aggregationForGroup = AggregationType.COVERED_PERCENTAGE, coverageUnits = CoverageUnit.BRANCH)
				}
			}
			filters {
				excludes {
					packages("aeon.composeapp.generated", "dev.bitvictory.aeon.dependencyinjection", "dev.bitvictory.aeon.theme")
					annotatedBy("androidx.compose.runtime.Composable")
				}
			}
		}
	}
}

dependencies {
	androidTestImplementation(libs.androidx.ui.test)
	debugImplementation(libs.androidx.ui.test.manifest)
	implementation(libs.androidx.security.crypto.ktx)
}

