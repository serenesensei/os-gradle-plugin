package org.mesleepy.gradle.osdetector.core

import org.mesleepy.gradle.osdetector.extension.OsDetector
import org.mesleepy.gradle.osdetector.facade.FileOperationProvider
import org.mesleepy.gradle.osdetector.facade.SystemPropertyOperationProvider
import java.util.*

class DetectorImpl : Detector {

    val detectedProperties = Properties()

    constructor(
        classifierWithLikes: List<String>,
        sysPropOps: SystemPropertyOperationProvider,
        fsOps: FileOperationProvider
    ) : super(sysPropOps, fsOps) {
        detect(detectedProperties, classifierWithLikes)
    }

    constructor(classifierWithLikes: List<String>) {
        detect(detectedProperties, classifierWithLikes)
    }

    override fun log(message: String?) {
        OsDetector.logger.info(message)
    }

    override fun logProperty(name: String?, value: String?) {
        OsDetector.logger.info("$name=$value")
    }
}
