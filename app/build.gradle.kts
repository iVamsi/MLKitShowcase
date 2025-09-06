plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.vamsi.mlkitshowcase"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vamsi.mlkitshowcase"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // ML Kit
    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.barcode.scanning)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Permissions
    implementation(libs.accompanist.permissions)
    
    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
    
    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.48")
    
    // Debug implementations
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Jacoco Configuration for Test Coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        // Exclude Hilt generated classes
        "**/*_HiltModules*",
        "**/*_Provide*",
        "**/*_MembersInjector*",
        "**/Hilt_*",
        "**/*_Factory*",
        "**/*Module_*",
        // Exclude Compose generated classes
        "**/*\$\$serializer*",
        "**/*ComposableSingletons*",
        "**/*_Kt*"
    )

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec", "outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
    })
}

tasks.register("jacocoTestCoverageVerification", JacocoCoverageVerification::class) {
    dependsOn("jacocoTestReport")
    
    violationRules {
        rule {
            limit {
                minimum = "1.00".toBigDecimal() // 100% coverage required
            }
        }
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*_HiltModules*",
        "**/*_Provide*",
        "**/*_MembersInjector*",
        "**/Hilt_*",
        "**/*_Factory*",
        "**/*Module_*",
        "**/*\$\$serializer*",
        "**/*ComposableSingletons*",
        "**/*_Kt*"
    )

    classDirectories.setFrom(files(fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }))
}