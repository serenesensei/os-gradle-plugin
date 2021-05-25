package org.mesleepy.gradle.osdetector

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import org.mesleepy.gradle.osdetector.extension.OsDetector

class OsDetectorPluginTest : FunSpec({

    lateinit var project: Project
    lateinit var osdetector: OsDetector

    beforeEach {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(OsDetectorPlugin::class)
        osdetector = project.extensions.getByType()
    }

    test("Should detect properties.") {

        osdetector shouldNotBe null
        osdetector.os shouldNotBe null
        osdetector.arch shouldNotBe null
        (osdetector.os + '-' + osdetector.arch).shouldBe(osdetector.classifier)

        System.err.println("classifier=" + osdetector.classifier)

        if (osdetector.os == "linux") {
            osdetector.release?.id shouldNotBe null
            System.err.println("release.id=" + osdetector.release?.id)
            System.err.println("release.version=" + osdetector.release?.version)
            System.err.println("release.isLike(debian)=" + osdetector.release?.isLike("debian"))
            System.err.println("release.isLike(redhat)=" + osdetector.release?.isLike("redhat"))
        } else if (osdetector.release != null) {
            fail("Should be null")
        }
    }

    test("Should set classifier with likes.") {
        osdetector.setClassifierWithLikes(listOf("debian", "fedora"))

        osdetector.os shouldNotBe null
        osdetector.arch shouldNotBe null
        System.err.println("classifier=" + osdetector.classifier)
        shouldThrowExactly<IllegalStateException> {
            osdetector.setClassifierWithLikes(listOf("debian"))
        }
    }
})
