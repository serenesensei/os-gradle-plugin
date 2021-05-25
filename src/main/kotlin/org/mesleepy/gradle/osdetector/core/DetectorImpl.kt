package org.mesleepy.gradle.osdetector.core

import org.mesleepy.gradle.osdetector.extension.OsDetectorExtension
import org.mesleepy.gradle.osdetector.facade.FileOperationProvider
import org.mesleepy.gradle.osdetector.facade.SystemPropertyOperationProvider
import org.slf4j.LoggerFactory
import java.util.*

class DetectorImpl : Detector {

    val detectedProperties = Properties()

    private val logger = LoggerFactory.getLogger(OsDetectorExtension::class.simpleName)

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
        logger.info(message)
    }

    override fun logProperty(name: String?, value: String?) {
        logger.info("$name=$value")
    }
}
