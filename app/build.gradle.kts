plugins {
    id("com.android.application")
}

val appId = "io.github.kouzen.kzvolume"

// VERSIONING
val appVersionName = "1.4.0"
val appVersionCode = 10

// Extract versions from appVersionName
val versionParts = appVersionName.split(".")
val versionMajor = versionParts.getOrNull(0)?.toIntOrNull() ?: 0
val versionMinor = versionParts.getOrNull(1)?.toIntOrNull() ?: 0
val versionPatch = versionParts.getOrNull(2)?.toIntOrNull() ?: 0

// Build number from environment
val versionBuild = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

// SIGNING - Optional (only if available)
val keystorePassword =
    System.getenv("KEYSTORE_PASSWORD") ?: ""

val keyAliasEnv =
    System.getenv("KEY_ALIAS") ?: ""

val keyPasswordEnv =
    System.getenv("KEY_PASSWORD") ?: ""

val hasReleaseSigning =
    keystorePassword.isNotBlank() &&
    keyAliasEnv.isNotBlank() &&
    keyPasswordEnv.isNotBlank()

// GRADLE CHECK
gradle.taskGraph.whenReady {
    val isGitHubActions = !System.getenv("GITHUB_ACTIONS").isNullOrBlank()
    val releaseTaskNames = setOf("assembleRelease", "bundleRelease", "packageRelease")
    val isReleaseTask = allTasks.any { task ->
        releaseTaskNames.any { it.equals(task.name, ignoreCase = true) }
    }

    // Fail in GitHub Actions if keystore is missing
    if (isReleaseTask && !hasReleaseSigning && isGitHubActions) {
        error(
            "Missing release signing environment variables in GitHub Actions. " +
            "Provide KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD to build signed releases."
        )
    }
}

// TASK - Print app info for CI/CD
tasks.register("printAppInfo") {
    doLast {
        println(
            """
            {
              "applicationId": "$appId",
              "versionMajor": $versionMajor,
              "versionMinor": $versionMinor,
              "versionPatch": $versionPatch,
              "versionBuild": $versionBuild,
              "version": "$appVersionName",
              "versionCode": $appVersionCode
            }
            """.trimIndent()
        )
    }
}

// ANDROID CONFIG
android {

    namespace = appId

    compileSdk = 37

    defaultConfig {

        applicationId = appId

        minSdk = 29
        targetSdk = 37

        versionCode = appVersionCode
        versionName = appVersionName
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }

    signingConfigs {

        create("release") {
            val localKeystore = file("kzvolume.jks")
            if (localKeystore.exists()) {
                storeFile = localKeystore
                storePassword = keystorePassword.ifBlank { "kzvolume" }
                keyAlias = keyAliasEnv.ifBlank { "kzvolume" }
                keyPassword = keyPasswordEnv.ifBlank { "kzvolume" }
            } else {
                storeFile = file("release.jks")
                storePassword = keystorePassword
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    buildTypes {

        debug {

            applicationIdSuffix = ".debug"

            versionNameSuffix = ".$versionBuild-debug"

            resValue(
                "string",
                "app_name",
                "kzvolume Debug"
            )
        }

        release {

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    androidComponents {
        onVariants(selector().withBuildType("release")) { variant ->
            variant.outputs.forEach { output ->
                val abi = output.filters.find { it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI }?.identifier
                val fileName = if (abi != null) "kzvolume-$abi.apk" else "kzvolume-universal.apk"
                output.outputFileName.set(fileName)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        resValues = true
    }

    packaging {
        resources {

            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/gradle/**"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)

    }
}

// DEPENDENCIES
dependencies {

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.appcompat)

    implementation(libs.material)

    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.viewpager2)

    implementation(libs.androidx.preference.ktx)

    implementation(libs.androidx.activity.ktx)

    implementation(libs.androidx.fragment.ktx)
}
