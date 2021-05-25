package org.mesleepy.gradle.osdetector

import org.mesleepy.gradle.osdetector.extension.OsDetectorExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class OsDetectorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<OsDetectorExtension>(EXTENSION_NAME, target)
    }

    companion object {
        private const val EXTENSION_NAME = "osdetector"
    }
}
