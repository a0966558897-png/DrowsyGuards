// backend/settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 僅允許在 settings 宣告倉庫，避免與 build.gradle.kts 衝突
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        // 如未來需要可在這裡加 google() / jcenter()（通常不需要）
    }
}

rootProject.name = "backend"
