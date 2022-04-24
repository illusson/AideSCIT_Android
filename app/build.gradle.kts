@file:Suppress("PropertyName", "LocalVariableName")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.internal.os.OperatingSystem
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("kotlin-parcelize")
//    id("kotlin-kapt")
}

val GIT_HEAD: String get() = Runtime.getRuntime()
    .exec("git rev-parse --short HEAD")
    .inputStream.reader().readLines()[0]

val GITHUB_REPO: String get() {
    val remote = Runtime.getRuntime()
        .exec("git remote get-url origin")
        .inputStream.reader().readText()
    val repo = Pattern.compile("github.com/(.*?).git")
    val matcher = repo.matcher(remote)
    if (!matcher.find()) {
        throw IllegalStateException()
    }
    val result = matcher.group(0)
    return result.substring(11, result.length - 4)
}

val DATED_VERSION: Int get() = Integer.parseInt(
    SimpleDateFormat("yyMMdd", Locale.CHINA).format(Date())
)

val COMMIT_VERSION: Int get() {
    return Runtime.getRuntime()
        .exec("git log -n 1 --pretty=format:%cd --date=format:%y%m%d")
        .inputStream.reader().readLines()[0]
        .toInt()
}

val TIME_MD5: String get() {
    val md5 = MessageDigest.getInstance("MD5")
    val ts = System.currentTimeMillis().toString().toByteArray()
    val digest = md5.digest(ts)
    val pre = BigInteger(1, digest)
    return pre.toString(16)
        .padStart(32, '0')
        .substring(8, 18)
}

val TYPE_RELEASE: String get() = "release"
val TYPE_DEBUG: String get() = "debug"
val TYPE_DEV: String get() = "dev"
val TYPE_SNAPSHOT: String get() = "snapshot"

val SIGN_CONFIG: String get() = "sign"

val VERSION_PROPERTIES get() =
    File(rootDir, "version.properties").apply {
        if (!exists()) {
            createNewFile()
        }
    }

android {
    compileSdk = 32
    buildToolsVersion = "32.1.0-rc1"

    val SIGN_EXIST: Boolean = file("./gradle.properties").exists()
    if (SIGN_EXIST) {
        signingConfigs {
            @Suppress("LocalVariableName")
            create(SIGN_CONFIG) {
                val SIGN_DIR: String by project
                val SIGN_PASSWORD_STORE: String by project
                val SIGN_ALIAS: String by project
                val SIGN_PASSWORD_KEY: String by project
                storeFile = file(SIGN_DIR)
                storePassword = SIGN_PASSWORD_STORE
                keyAlias = SIGN_ALIAS
                keyPassword = SIGN_PASSWORD_KEY
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "io.github.sgpublic.aidescit"
        minSdk = 26
        targetSdk = 32
        versionCode = COMMIT_VERSION
        versionName = "2.0.0"

        renderscriptTargetApi = 26
        renderscriptSupportModeEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11")
            }
        }
        ndk.abiFilters.run {
            add("arm64-v8a")
            add("x86_64")
        }

        fun buildConfigStringField(name: String, value: String) {
            buildConfigField("String", name, "\"$value\"")
        }
        GITHUB_REPO.let {
            buildConfigStringField("GITHUB_REPO", it)
            val repo = it.split("/")
            buildConfigStringField("GITHUB_AUTHOR", repo[0])
            buildConfigStringField("GITHUB_REPO_NAME", repo[1])
        }
        buildConfigStringField("PROJECT_NAME", rootProject.name)
        buildConfigStringField("TYPE_RELEASE", TYPE_RELEASE)
        buildConfigStringField("TYPE_DEV", TYPE_DEV)
        buildConfigStringField("TYPE_SNAPSHOT", TYPE_SNAPSHOT)
        buildConfigStringField("TYPE_DEBUG", TYPE_DEBUG)
    }

    buildTypes {
        val versionProps = Properties().apply {
            load(VERSION_PROPERTIES.inputStream())
        }

        all {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(SIGN_CONFIG)
        }

        /** 自动化版本命名 */
        getByName(TYPE_RELEASE) {
            versionNameSuffix = "-$name"
            isDebuggable = false
            versionProps[TYPE_RELEASE] = "${rootProject.name} V${
                defaultConfig.versionName
            }(${defaultConfig.versionCode})"
        }
        getByName(TYPE_DEBUG) {
            defaultConfig.versionCode = DATED_VERSION
            applicationIdSuffix = ".$TYPE_DEBUG"
            isDebuggable = true
            versionNameSuffix = "-$TIME_MD5-$name"
        }
        register(TYPE_DEV) {
            versionNameSuffix = "-$GIT_HEAD-$name"
            isDebuggable = true
            isTestCoverageEnabled = true
            versionProps[TYPE_DEV] = "${rootProject.name}_${
                defaultConfig.versionName
            }_$GIT_HEAD"
        }
        register(TYPE_SNAPSHOT) {
            defaultConfig.versionCode = DATED_VERSION
            isDebuggable = true
            val suffix = TIME_MD5
            versionNameSuffix = "-$suffix-$name"
            versionProps[TYPE_SNAPSHOT] = "${rootProject.name}_${
                defaultConfig.versionName
            }_$suffix"
        }

        versionProps.store(VERSION_PROPERTIES.writer(), null)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp_private/CMakeLists.txt")
                .takeIf { it.exists() }
                ?: file("src/main/cpp/CMakeLists.txt")
            version = "3.18.1"
        }
    }
    ndkVersion = "24.0.8215888"
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")
    implementation("com.facebook.rebound:rebound:0.3.8")
    implementation("com.yanzhenjie:sofia:1.0.5")
    implementation("com.github.SGPublic:Blur-Fix-AndroidX:1.1.2")
    implementation("com.github.SGPublic:SwipeBackLayoutX:1.2.1")
    implementation("com.github.SGPublic:MultiWaveHeaderX:1.0.0")
    implementation("com.github.zhpanvip.BannerViewPager:bannerview:2.6.6")
    implementation("com.ogaclejapan.smarttablayout:library:2.0.0@aar")
    implementation("com.github.li-xiaojun:XPopup:2.7.7")
    implementation("com.github.getActivity:XXPermissions:13.5")
}

/** 自动修改输出文件名并定位文件 */
android.applicationVariants.all {
    outputs.forEach {
        if (it.name == "debug") {
            return@forEach
        }
        (it as BaseVariantOutputImpl).outputFileName = "${Properties().apply {
            load(VERSION_PROPERTIES.inputStream())
        }[it.name] as String}.apk"

        val name = it.name.let { name ->
            return@let StringBuilder(name.length)
                .append(Character.toTitleCase(name[0]))
                .append(name, 1, name.length)
                .toString()
        }
        tasks.create("package${name}AndLocate") {
            dependsOn("assemble$name")
            doLast {
                val path = it.outputFile.absolutePath
                if (!File(path).exists()) {
                    return@doLast
                }
                when (true) {
                    OperatingSystem.current().isWindows ->
                        Runtime.getRuntime().exec("explorer.exe /select, $path")
                }
            }
        }
    }
}
