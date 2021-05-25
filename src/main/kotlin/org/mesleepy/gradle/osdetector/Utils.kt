package org.mesleepy.gradle.osdetector

import org.gradle.api.Project
import org.gradle.util.VersionNumber

object Utils {

    fun compareGradleVersion(
        project: Project,
        target: String
    ): Int = VersionNumber
        .parse(project.gradle.gradleVersion)
        .compareTo(VersionNumber.parse(target))
}
