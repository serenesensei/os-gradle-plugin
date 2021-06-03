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

package org.serenesensai.gradle.osdetector.extension

import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.serenesensai.gradle.osdetector.Utils
import org.serenesensai.gradle.osdetector.core.Detector
import org.serenesensai.gradle.osdetector.core.DetectorImpl
import org.serenesensai.gradle.osdetector.core.Release
import org.serenesensai.gradle.osdetector.facade.ConfigurationTimeSafeFileOperations
import org.serenesensai.gradle.osdetector.facade.ConfigurationTimeSafeSystemPropertyOperations
import javax.inject.Inject

abstract class OsDetectorExtension(private val project: Project) {

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @get:Inject
    abstract val projectLayout: ProjectLayout

    private val classifierWithLikes = mutableListOf<String>()
    private var detectorImpl: DetectorImpl? = null
    val os: String
        get() = getImpl().detectedProperties[Detector.DETECTED_NAME] as String
    val arch: String
        get() = getImpl().detectedProperties[Detector.DETECTED_ARCH] as String
    val classifier: String
        get() = getImpl().detectedProperties[Detector.DETECTED_CLASSIFIER] as String
    val release: Release?
        get() {
            val impl = getImpl()
            impl.detectedProperties[Detector.DETECTED_RELEASE] ?: return null

            return Release(impl)
        }

    @Synchronized
    private fun getImpl(): DetectorImpl {
        if (detectorImpl == null) {
            // Config cache is supported in Gradle 6.5 and above.
            detectorImpl = if (Utils.compareGradleVersion(project, "6.5") >= 0) {
                DetectorImpl(
                    classifierWithLikes = classifierWithLikes,
                    sysPropOps = ConfigurationTimeSafeSystemPropertyOperations(providerFactory),
                    fsOps = ConfigurationTimeSafeFileOperations(projectLayout, providerFactory)
                )
            } else DetectorImpl(classifierWithLikes)
        }

        return detectorImpl as DetectorImpl
    }

    @Synchronized
    fun setClassifierWithLikes(classifierWithLikes: List<String>) {
        if (detectorImpl != null) {
            throw IllegalStateException(
                "classifierWithLikes must be set before osdetector is read."
            )
        }
        this.classifierWithLikes.clear()
        this.classifierWithLikes.addAll(classifierWithLikes)
    }
}
