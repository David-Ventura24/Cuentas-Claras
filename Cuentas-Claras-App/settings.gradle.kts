pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()


        maven { url = java.net.URI("https://androidx.dev/storage/compose-compiler/repository") }

        maven { url = java.net.URI("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
}

rootProject.name = "Cuentas-Claras-App"
include(":app")