pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") // 新增 JitPack 仓库
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
<<<<<<< Updated upstream
        maven("https://jitpack.io") // 新增 JitPack 仓库
=======
        maven(url = "https://jitpack.io")
>>>>>>> Stashed changes
    }
}

rootProject.name = "Demo App"
include(":app")
