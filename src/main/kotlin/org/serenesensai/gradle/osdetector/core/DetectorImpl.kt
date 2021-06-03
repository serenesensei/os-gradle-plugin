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
package org.serenesensai.gradle.osdetector.core

import org.serenesensai.gradle.osdetector.extension.OsDetectorExtension
import org.serenesensai.gradle.osdetector.facade.FileOperationProvider
import org.serenesensai.gradle.osdetector.facade.SystemPropertyOperationProvider
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
