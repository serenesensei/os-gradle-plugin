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

package org.mesleepy.gradle.osdetector.core

import org.mesleepy.gradle.osdetector.exception.DetectionException
import org.mesleepy.gradle.osdetector.facade.FileOperationProvider
import org.mesleepy.gradle.osdetector.facade.SimpleFileOperations
import org.mesleepy.gradle.osdetector.facade.SimpleSystemPropertyOperations
import org.mesleepy.gradle.osdetector.facade.SystemPropertyOperationProvider
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class Detector(
    private val systemPropertyOperationProvider: SystemPropertyOperationProvider = SimpleSystemPropertyOperations(),
    private val fsOps: FileOperationProvider = SimpleFileOperations()
) {

    protected abstract fun log(message: String?)
    protected abstract fun logProperty(name: String?, value: String?)

    internal fun detect(properties: Properties, classifierWithLikes: List<String>) {
        val osName = systemPropertyOperationProvider.getSystemProperty("os.name")
        val osArch = systemPropertyOperationProvider.getSystemProperty("os.arch")
        val osVersion = systemPropertyOperationProvider.getSystemProperty("os.version")

        val detectedName: String = normalizeOs(osName)
        val detectedArch: String = normalizeArch(osArch)
        val detectedBitness: Int = determineBitness(detectedArch)

        setProperty(properties, DETECTED_NAME, detectedName)
        setProperty(properties, DETECTED_ARCH, detectedArch)
        setProperty(properties, DETECTED_BITNESS, "" + detectedBitness)

        val versionMatcher: Matcher = VERSION_REGEX.matcher(osVersion)
        if (versionMatcher.matches()) {
            setProperty(properties, DETECTED_VERSION, versionMatcher.group(1))
            setProperty(properties, DETECTED_VERSION_MAJOR, versionMatcher.group(2))
            setProperty(properties, DETECTED_VERSION_MINOR, versionMatcher.group(3))
        }

        val failOnUnknownOS = systemPropertyOperationProvider.getSystemProperty("failOnUnknownOS")
        if (!"false".equals(failOnUnknownOS, ignoreCase = true)) {
            if (UNKNOWN == detectedName) {
                throw DetectionException("unknown os.name: $osName")
            }
            if (UNKNOWN == detectedArch) {
                throw DetectionException("unknown os.arch: $osArch")
            }
        }

        // Assume the default classifier, without any os "like" extension.
        val detectedClassifierBuilder = StringBuilder()
        detectedClassifierBuilder.append(detectedName)
        detectedClassifierBuilder.append('-')
        detectedClassifierBuilder.append(detectedArch)

        // For Linux systems, add additional properties regarding details of the OS.
        val linuxRelease: LinuxRelease? = if ("linux" == detectedName) getLinuxRelease() else null
        if (linuxRelease != null) {
            setProperty(properties, DETECTED_RELEASE, linuxRelease.id)
            if (linuxRelease.version != null) {
                setProperty(properties, DETECTED_RELEASE_VERSION, linuxRelease.version)
            }

            // Add properties for all systems that this OS is "like".
            linuxRelease.like.forEach {
                setProperty(properties, DETECTED_RELEASE_LIKE_PREFIX + it, "true")
            }

            // If any of the requested classifier likes are found in the "likes" for this system,
            // append it to the classifier.
            classifierWithLikes
                .filter { linuxRelease.like.contains(it) }
                .forEach {
                    detectedClassifierBuilder.append('-')
                    detectedClassifierBuilder.append(it)
                    // First one wins.
                    return@forEach
                }
        }
        setProperty(properties, DETECTED_CLASSIFIER, detectedClassifierBuilder.toString())
    }

    private fun getLinuxRelease(): LinuxRelease? {
        // First, look for the os-release file.
        LINUX_OS_RELEASE_FILES.forEach {
            val res: LinuxRelease? = parseLinuxOsReleaseFile(it)
            if (res != null) {
                return res
            }
        }

        // Older versions of redhat don't have /etc/os-release. In this case, try
        // parsing this file.
        return parseLinuxRedhatReleaseFile(REDHAT_RELEASE_FILE)
    }

    private fun parseLinuxOsReleaseFile(fileName: String): LinuxRelease? {
        var reader: BufferedReader? = null
        var id: String? = null
        var version: String? = null
        val likeSet = linkedSetOf<String>()

        try {
            val inputStream = fsOps.readFile(fileName)
            reader = inputStream!!.bufferedReader(StandardCharsets.UTF_8)

            reader.forEachLine { line ->
                when {
                    // Parse the ID line.
                    line.startsWith(LINUX_ID_PREFIX) -> {
                        // Set the ID for this version.
                        id = normalizeOsReleaseValue(line.substring(LINUX_ID_PREFIX.length))

                        // Also add the ID to the "like" set.
                        likeSet.add(id!!)
                    }
                    // Parse the VERSION_ID line.
                    line.startsWith(LINUX_VERSION_ID_PREFIX) -> {
                        // Set the ID for this version.
                        version = normalizeOsReleaseValue(
                            line.substring(LINUX_VERSION_ID_PREFIX.length)
                        )
                    }
                    // Parse the ID_LIKE line.
                    line.startsWith(LINUX_ID_LIKE_PREFIX) -> {
                        // Split the line on any whitespace.
                        val parts = normalizeOsReleaseValue(
                            line.substring(LINUX_ID_LIKE_PREFIX.length)
                        ).split("\\s+")

                        likeSet.addAll(parts)
                    }
                }
            }
        } catch (ignored: IOException) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader)
        }

        return id?.let { LinuxRelease(id!!, version, likeSet) }
    }

    private fun parseLinuxRedhatReleaseFile(
        @Suppress("SameParameterValue") fileName: String
    ): LinuxRelease? {
        var reader: BufferedReader? = null
        var id: String? = null
        var version: String? = null
        val likeSet = linkedSetOf(*DEFAULT_REDHAT_VARIANTS)

        try {
            val inputStream = fsOps.readFile(fileName)
            reader = inputStream!!.bufferedReader(StandardCharsets.UTF_8)

            reader.forEachLine {
                val line = it.toLowerCase(Locale.US)
                id = when {
                    line.contains("centos") -> {
                        "centos"
                    }
                    line.contains("fedora") -> {
                        "fedora"
                    }
                    line.contains("red hat enterprise linux") -> {
                        "rhel"
                    }
                    else -> {
                        // Other variants are not currently supported.
                        return@forEachLine
                    }
                }
                val versionMatcher = REDHAT_MAJOR_VERSION_REGEX.matcher(line)
                if (versionMatcher.find()) {
                    version = versionMatcher.group(1)
                }
                likeSet.add(id!!)
            }
        } catch (ignored: IOException) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader)
        }

        return id?.let { LinuxRelease(id!!, version, likeSet) }
    }

    private fun closeQuietly(obj: Closeable?) {
        try {
            obj?.close()
        } catch (ignored: IOException) {
            // Ignore.
        }
    }

    // Remove any quotes from the string.
    private fun normalizeOsReleaseValue(value: String) =
        value.trim { it <= ' ' }.replace("\"", "")

    private fun normalizeOs(osName: String?): String {
        val value = normalize(osName)

        return when {
            value.startsWith("aix") -> "aix"
            value.startsWith("hpux") -> "hpux"
            // Avoid the names such as os4000
            value.startsWith("os400") ->
                return if (value.length <= 5 || !Character.isDigit(value[5])) "os400" else "os400"
            value.startsWith("linux") -> "linux"
            value.startsWith("macosx") || value.startsWith("osx") -> "osx"
            value.startsWith("freebsd") -> "freebsd"
            value.startsWith("openbsd") -> "openbsd"
            value.startsWith("netbsd") -> "netbsd"
            value.startsWith("solaris") || value.startsWith("sunos") -> "sunos"
            value.startsWith("windows") -> "windows"
            value.startsWith("zos") -> "zos"
            else -> UNKNOWN
        }
    }

    private fun normalizeArch(osArch: String?): String {
        val value = normalize(osArch)

        return when {
            value.matches("^(x8664|amd64|ia32e|em64t|x64)$".toRegex()) -> "x86_64"
            value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$".toRegex()) -> "x86_32"
            value.matches("^(ia64w?|itanium64)$".toRegex()) -> "itanium_64"
            "ia64n" == value -> "itanium_32"
            value.matches("^(sparc|sparc32)$".toRegex()) -> "sparc_32"
            value.matches("^(sparcv9|sparc64)$".toRegex()) -> "sparc_64"
            value.matches("^(arm|arm32)$".toRegex()) -> "arm_32"
            "aarch64" == value -> "aarch_64"
            value.matches("^(mips|mips32)$".toRegex()) -> "mips_32"
            value.matches("^(mipsel|mips32el)$".toRegex()) -> "mipsel_32"
            "mips64" == value -> "mips_64"
            "mips64el" == value -> "mipsel_64"
            value.matches("^(ppc|ppc32)$".toRegex()) -> "ppc_32"
            value.matches("^(ppcle|ppc32le)$".toRegex()) -> "ppcle_32"
            "ppc64" == value -> "ppc_64"
            "ppc64le" == value -> "ppcle_64"
            "s390" == value -> "s390_32"
            "s390x" == value -> "s390_64"
            "riscv" == value -> "riscv"
            else -> UNKNOWN
        }
    }

    private fun normalize(value: String?): String =
        value?.toLowerCase(Locale.US)?.replace("[^a-z0-9]+".toRegex(), "") ?: ""

    private fun determineBitness(architecture: String): Int {
        // try the widely adopted sun specification first.
        var bitness = systemPropertyOperationProvider.getSystemProperty("sun.arch.data.model", "")

        if (!bitness.isNullOrEmpty() && bitness.matches("[0-9]+".toRegex())) {
            return bitness.toInt(10)
        }

        // bitness from sun.arch.data.model cannot be used. Try the IBM specification.
        bitness = systemPropertyOperationProvider.getSystemProperty("com.ibm.vm.bitmode", "")

        return if (!bitness.isNullOrEmpty() && bitness.matches("[0-9]+".toRegex())) {
            bitness.toInt(10)
        } else {
            // as a last resort, try to determine the bitness from the architecture.
            guessBitnessFromArchitecture(architecture)
        }
    }

    private fun guessBitnessFromArchitecture(arch: String) = if (arch.contains("64")) 64 else 32

    private fun setProperty(
        props: Properties,
        name: String,
        value: String
    ) {
        props[name] = value
        systemPropertyOperationProvider.setSystemProperty(name, value)
    }

    private class LinuxRelease(
        val id: String,
        val version: String?,
        val like: LinkedHashSet<String>
    )

    companion object {
        internal const val DETECTED_NAME = "os.detected.name"
        internal const val DETECTED_ARCH = "os.detected.arch"
        internal const val DETECTED_CLASSIFIER = "os.detected.classifier"

        private const val DETECTED_BITNESS = "os.detected.bitness"
        private const val DETECTED_VERSION = "os.detected.version"
        private const val DETECTED_VERSION_MAJOR = "$DETECTED_VERSION.major"
        private const val DETECTED_VERSION_MINOR = "$DETECTED_VERSION.minor"
        internal const val DETECTED_RELEASE = "os.detected.release"
        internal const val DETECTED_RELEASE_VERSION = "$DETECTED_RELEASE.version"
        internal const val DETECTED_RELEASE_LIKE_PREFIX = "$DETECTED_RELEASE.like."

        private const val UNKNOWN = "unknown"
        private const val LINUX_ID_PREFIX = "ID="
        private const val LINUX_ID_LIKE_PREFIX = "ID_LIKE="
        private const val LINUX_VERSION_ID_PREFIX = "VERSION_ID="
        private val LINUX_OS_RELEASE_FILES = arrayOf("/etc/os-release", "/usr/lib/os-release")
        private const val REDHAT_RELEASE_FILE = "/etc/redhat-release"
        private val DEFAULT_REDHAT_VARIANTS = arrayOf("rhel", "fedora")

        private val VERSION_REGEX = Pattern.compile("((\\d+)\\.(\\d+)).*")
        private val REDHAT_MAJOR_VERSION_REGEX = Pattern.compile("(\\d+)")
    }
}
