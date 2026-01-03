package com.adgainai.springbooturlfullpath.gutter.curl


import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Spring Boot 端口解析器（无 PSI / 无 Properties 插件依赖）
 *
 * 优先级：
 * 1. server.port（RunConfig: args / vm / env）
 * 2. 当前 active 的 application*.properties / yml
 * 3. 默认 8080
 */
object SpringBootPortResolver {

    fun resolve(project: Project): Int {
        return fromExplicitServerPort(project)
            ?: fromActiveApplicationConfig(project)
            ?: 8080
    }

    // ------------------------------------------------------------------------
    // 1️⃣ RunConfiguration 中显式 server.port
    // ------------------------------------------------------------------------

    private fun fromExplicitServerPort(project: Project): Int? {
        val runManager = RunManager.getInstance(project)

        runManager.allSettings
            .map { it.configuration }
            .filterIsInstance<RunConfigurationBase<*>>()
            .forEach { cfg ->

                extractInt(
                    readString(cfg, "programParameters"),
                    """--server\.port=(\d+)"""
                )?.let { return it }

                extractInt(
                    readString(cfg, "vmParameters"),
                    """-Dserver\.port=(\d+)"""
                )?.let { return it }

                readEnv(cfg)["SERVER_PORT"]
                    ?.toIntOrNull()
                    ?.let { return it }
            }

        return null
    }

    // ------------------------------------------------------------------------
    // 2️⃣ application.properties / yml（按 active profile）
    // ------------------------------------------------------------------------

    private fun fromActiveApplicationConfig(project: Project): Int? {
        val profiles = resolveActiveProfiles(project)
        val scope = GlobalSearchScope.projectScope(project)

        val fileNames = mutableListOf<String>()

        profiles.forEach { profile ->
            fileNames += "application-$profile.properties"
            fileNames += "application-$profile.yml"
            fileNames += "application-$profile.yaml"
        }

        fileNames += listOf(
            "application.properties",
            "application.yml",
            "application.yaml"
        )

        for (name in fileNames) {
            val files = FilenameIndex.getVirtualFilesByName(project, name, scope)
            for (vf in files) {
                readPortFromVirtualFile(vf)?.let { return it }
            }
        }

        return null
    }

    // ------------------------------------------------------------------------
    // active profiles
    // ------------------------------------------------------------------------

    private fun resolveActiveProfiles(project: Project): List<String> {
        val runManager = RunManager.getInstance(project)

        runManager.allSettings
            .map { it.configuration }
            .filterIsInstance<RunConfigurationBase<*>>()
            .forEach { cfg ->

                extractString(
                    readString(cfg, "programParameters"),
                    """--spring\.profiles\.active=([\w,-]+)"""
                )?.let { return it.split(",") }

                extractString(
                    readString(cfg, "vmParameters"),
                    """-Dspring\.profiles\.active=([\w,-]+)"""
                )?.let { return it.split(",") }

                readEnv(cfg)["SPRING_PROFILES_ACTIVE"]
                    ?.let { return it.split(",") }
            }

        return listOf("default")
    }

    // ------------------------------------------------------------------------
    // VirtualFile 解析（纯文本）
    // ------------------------------------------------------------------------

    private fun readPortFromVirtualFile(vf: VirtualFile): Int? {
        val text = runCatching { VfsUtilCore.loadText(vf) }.getOrNull()
            ?: return null

        return when {
            vf.name.endsWith(".properties") ->
                readFromPropertiesText(text)

            vf.name.endsWith(".yml") || vf.name.endsWith(".yaml") ->
                readFromYamlText(text)

            else -> null
        }
    }

    private fun readFromPropertiesText(text: String): Int? {
        // 支持：
        // server.port=8081
        val regex = Regex("""^\s*server\.port\s*=\s*(\d+)\s*$""", RegexOption.MULTILINE)
        return regex.find(text)
            ?.groupValues
            ?.get(1)
            ?.toInt()
    }

    private fun readFromYamlText(text: String): Int? {
        // 支持：
        // server:
        //   port: 8081
        val regex = Regex(
            """server:\s*[\r\n]+\s*port:\s*(\d+)""",
            RegexOption.MULTILINE
        )
        return regex.find(text)
            ?.groupValues
            ?.get(1)
            ?.toInt()
    }

    // ------------------------------------------------------------------------
    // 工具方法（RunConfig 反射）
    // ------------------------------------------------------------------------

    private fun extractInt(text: String?, regex: String): Int? {
        if (text.isNullOrBlank()) return null
        return Regex(regex)
            .find(text)
            ?.groupValues
            ?.get(1)
            ?.toInt()
    }

    private fun extractString(text: String?, regex: String): String? {
        if (text.isNullOrBlank()) return null
        return Regex(regex)
            .find(text)
            ?.groupValues
            ?.get(1)
    }

    private fun readString(cfg: Any, name: String): String? =
        runCatching {
            cfg.javaClass
                .getMethod("get${name.replaceFirstChar { it.uppercase() }}")
                .invoke(cfg) as? String
        }.getOrNull()

    @Suppress("UNCHECKED_CAST")
    private fun readEnv(cfg: Any): Map<String, String> {
        val env = runCatching {
            cfg.javaClass
                .getMethod("getEnvs")
                .invoke(cfg) as? Map<String, String>
        }.getOrNull()

        return env ?: emptyMap()
    }
}
