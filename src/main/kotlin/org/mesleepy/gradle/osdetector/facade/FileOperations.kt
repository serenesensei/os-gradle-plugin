/*
 * Copyright 2021 me-sleepy.
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

package org.mesleepy.gradle.osdetector.facade

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

/** Provides simple fs operations. (Gradle version < 6.5) */
internal class SimpleFileOperations : FileOperationProvider {

    override fun readFile(filePath: String): InputStream = FileInputStream(filePath)
}

/** Provides fs operations compatible with Gradle configuration cache. (Gradle version >= 6.5)  */
internal class ConfigurationTimeSafeFileOperations(
    private val projectLayout: ProjectLayout,
    private val providerFactory: ProviderFactory
) : FileOperationProvider {

    override fun readFile(filePath: String): InputStream = providerFactory
        .fileContents(projectLayout.projectDirectory.file(filePath))
        .asBytes
        .forUseAtConfigurationTime()
        .orNull
        ?.inputStream() ?: throw FileNotFoundException("$filePath does not exist.")
}
