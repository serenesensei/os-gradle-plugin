package org.mesleepy.gradle.osdetector.facade

import java.io.IOException
import java.io.InputStream

/**
 * Interface exposing file operations.
 */
interface FileOperationProvider {
    /**
     * Gets a [InputStream] for reading the content of the file with the specified path.
     *
     * @param      filePath   the system-dependent file path.
     * @return     the [InputStream] that can be read to get the file content.
     * @throws     IOException if the file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading.
     */
    fun readFile(filePath: String): InputStream?
}
