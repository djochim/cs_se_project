rootProject.name = "aeon"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/djochim/openai-kotlin")
			credentials {
				username = System.getenv("GITHUB_PACKAGES_USER")
				password = System.getenv("GITHUB_PACKAGES_PW")
			}
		}
		mavenCentral()
		mavenLocal()
	}
}

include(":composeApp")
include(":server")
include(":shared")