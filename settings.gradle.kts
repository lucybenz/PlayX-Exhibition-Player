pluginManagement {
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/google/")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/gradle-plugin/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://mirrors.cloud.tencent.com/nexus/repository/google/")
        google()
        mavenCentral()
    }
}

rootProject.name = "PlayX"
include(":app")