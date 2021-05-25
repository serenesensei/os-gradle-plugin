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
