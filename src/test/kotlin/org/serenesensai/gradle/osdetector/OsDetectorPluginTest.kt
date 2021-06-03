/*
 * Copyright (C) 2021 serenesensei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.serenesensai.gradle.osdetector

import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.testfixtures.ProjectBuilder
import org.serenesensai.gradle.osdetector.extension.OsDetectorExtension

class OsDetectorPluginTest : FunSpec({

    lateinit var project: Project
    lateinit var osdetector: OsDetectorExtension

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
