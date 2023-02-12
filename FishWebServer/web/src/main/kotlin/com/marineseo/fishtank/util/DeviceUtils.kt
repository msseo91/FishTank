package com.marineseo.fishtank.util

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object DeviceUtils {
    fun getDriver(devFile: File): String {
        val infoText = "udevadm info ${devFile.absolutePath}".runCommand()
        val driverToken = "ID_USB_DRIVER="

        return infoText.substring(
            infoText.indexOf(driverToken),
            infoText.indexOf("\n", infoText.indexOf(driverToken))
        ).replace(driverToken, "")
    }

    fun getFileList(path: String, glob: String): List<File> {
        val dir = Paths.get(path)
        val fileList = mutableListOf<File>()

        Files.newDirectoryStream(dir, glob).use { stream ->
            for (file in stream) {
                fileList.add(file.toFile())
            }
        }

        return fileList
    }
}