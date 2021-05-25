package org.mesleepy.gradle.osdetector.extension

import org.mesleepy.gradle.osdetector.core.Detector
import org.mesleepy.gradle.osdetector.core.DetectorImpl
import org.mesleepy.gradle.osdetector.core.Release
import org.mesleepy.gradle.osdetector.facade.ConfigurationTimeSafeFileOperations
import org.mesleepy.gradle.osdetector.facade.ConfigurationTimeSafeSystemPropertyOperations
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import org.gradle.util.VersionNumber
import org.slf4j.LoggerFactory
import javax.inject.Inject

abstract class OsDetector(private val project: Project) {

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
            detectorImpl = if (compareGradleVersion(project, "6.5") >= 0) {
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

    companion object {
        internal val logger = LoggerFactory.getLogger(OsDetector::class.java.name)

        private fun compareGradleVersion(
            project: Project,
            @Suppress("SameParameterValue") target: String
        ): Int = VersionNumber
            .parse(project.gradle.gradleVersion)
            .compareTo(VersionNumber.parse(target))
    }
}
