import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.GradleException
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

val localJvmJavaTestSourceDirectories = listOf("src/test/java")
val localJvmKotlinTestSourceDirectories = listOf(
    "src/test/kotlin",
    "src/propertyTest/kotlin",
)

val kotestReplaySeed = providers.gradleProperty("kotestSeed")
    .orElse(providers.environmentVariable("KOTEST_SEED"))
    .orNull
    ?.also { seed ->
        if (seed.toLongOrNull() == null) {
            throw GradleException("Kotest replay seed must be a signed 64-bit integer: '$seed'")
        }
    }

val kotestIterationCount = providers.gradleProperty("kotestIterations")
    .orElse(providers.environmentVariable("KOTEST_ITERATIONS"))
    .orElse("1000")
    .get()
    .toIntOrNull()
    ?.takeIf { it >= 100 }
    ?: throw GradleException("Kotest property tests require at least 100 successful iterations")

val kotestRunner = libs.kotest.runner.junit5
val kotestProperty = libs.kotest.property
val kotestAssertions = libs.kotest.assertions.core
val junitVintageEngine = libs.junit.vintage.engine
val junitPlatformLauncher = libs.junit.platform.launcher

subprojects {
    fun configureLocalJvmTestDependencies() {
        dependencies.add("testImplementation", kotestRunner)
        dependencies.add("testImplementation", kotestProperty)
        dependencies.add("testImplementation", kotestAssertions)
        dependencies.add("testRuntimeOnly", junitVintageEngine)
        dependencies.add("testRuntimeOnly", junitPlatformLauncher)
    }

    pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> {
            sourceSets.named("test") {
                java.setSrcDirs(localJvmJavaTestSourceDirectories)
                kotlin.setSrcDirs(localJvmKotlinTestSourceDirectories)
            }
        }
        configureLocalJvmTestDependencies()
    }

    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            sourceSets.named("test") {
                java.setSrcDirs(localJvmJavaTestSourceDirectories)
                kotlin.setSrcDirs(localJvmKotlinTestSourceDirectories)
            }
        }
        configureLocalJvmTestDependencies()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        // Kotest emits the seed and final shrunk counterexample on failure. CI can replay
        // with KOTEST_SEED=<seed> (or -PkotestSeed=<seed>) and archive this task-local cache.
        systemProperty("kotest.proptest.default.iteration.count", kotestIterationCount)
        systemProperty("kotest.proptest.output.shrink-steps", true)
        systemProperty("kotest.proptest.seed.write-failed", true)
        kotestReplaySeed?.let { seed ->
            systemProperty("kotest.proptest.default.seed", seed)
        }

        val kotestCacheDirectory = layout.buildDirectory.dir("kotest-cache/$name")
        environment("XDG_CACHE_HOME", kotestCacheDirectory.get().asFile.absolutePath)

        inputs.property("kotest.proptest.default.iteration.count", kotestIterationCount)
        inputs.property("kotest.proptest.default.seed", kotestReplaySeed ?: "random")

        reports.junitXml.required.set(true)
        reports.html.required.set(true)

        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = providers.environmentVariable("CI").isPresent
        }

        doFirst {
            logger.lifecycle(
                "Kotest seed cache for $path: ${kotestCacheDirectory.get().asFile}/.kotest/seeds"
            )
            kotestReplaySeed?.let { seed ->
                logger.lifecycle("Replaying Kotest property tests with seed $seed")
            }
        }
    }
}
