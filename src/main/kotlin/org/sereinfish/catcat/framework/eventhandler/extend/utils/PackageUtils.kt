package org.sereinfish.catcat.framework.eventhandler.extend.utils

import java.io.File
import java.util.jar.JarFile

object PackageUtils {

    /**
     * 扫描jar包下指定包名下的文件，并且返回类名列表
     */
    fun scanJar(jarFile: JarFile, packet: String): List<String> {
        // 遍历读取指定目录下文件
        return jarFile.entries().asSequence().filter { entry ->
                // 过滤指定目录
                entry.name.startsWith(packet.replace(".", "/"))
            }.filter { it.name.endsWith(".class") }.map { entry ->
                // 获取类名
                entry.name.substringBeforeLast(".").replace("/", ".")
            }.toList()
    }
}