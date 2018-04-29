/*
 * Copyright (c) 2017 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.okcheck

import com.liulishuo.okcheck.util.BuildConfig
import com.liulishuo.okcheck.util.ChangeFile
import com.liulishuo.okcheck.util.ChangeModule
import com.liulishuo.okcheck.util.DestinationUtil
import com.liulishuo.okcheck.util.Util
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class OkCheckTask extends DefaultTask {
    @Input
    List<String> changedModuleList

    @Input
    boolean isMock

    OkCheckTask() {
    }

    @TaskAction
    void setupOkcheck() {
        if (project == project.rootProject) {
            println "OkCheck: Finish root okcheck task!"
        } else if (!isMock) {
            println "OkCheck: Finish ${project.name} okcheck task!"
            BuildConfig.addToPassedModuleFile(project)

            if (BuildConfig.isAllModulePassed(project, changedModuleList)) {
                ChangeFile changeFile = new ChangeFile(project.rootProject)
                changeFile.refreshLastExecCommitId()
                println "OkCheck: All check is passed and refreshed the commit to current one!"
                println "OkCheck: ${changeFile.maintain()}"
            }
        }

    }

    static def addValidTask(Project project, List<String> moduleList, OkCheckExtension extension) {
        addValidTask(project, moduleList, extension, "", "")

        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }

        buildTypes.each { buildType ->
            addValidTask(project, moduleList, extension, "", "${buildType.capitalize()}")
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addValidTask(project, moduleList, extension, "${flavor.capitalize()}", "${buildType.capitalize()}")
                }
            }
        }
    }

    static def addMockTask(Project project) {
        addMockTask(project, "", "")

        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }

        buildTypes.each { buildType ->
            addMockTask(project, "", "${buildType.capitalize()}")
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addMockTask(project, "${flavor.capitalize()}", "${buildType.capitalize()}")
                }
            }
        }
    }

    static
    def addValidTask(Project project, List<String> moduleList, OkCheckExtension extension, String flavor, String buildType) {
        Set<String> dependsTaskNames = new HashSet<>()
        dependsTaskNames.add("lint$flavor$buildType")
        if (extension.enableCheckstyle) dependsTaskNames.add(OkCheckStyleTask.NAME)
        if (extension.enablePmd) dependsTaskNames.add(OkPmdTask.NAME)
        if (extension.enableFindbugs) dependsTaskNames.add("${OkFindbugsTask.NAME}$flavor$buildType")
        if (extension.enableKtlint) dependsTaskNames.add(OkKtlintTask.NAME)

        project.task(OkCheckPlugin.TASK_NAME + "$flavor$buildType", type: OkCheckTask, overwrite: true) {
            dependsOn dependsTaskNames
            setGroup("verification")
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Run check only for changed files for all variants")
            } else {
                setDescription("Run check only for changed files for $flavor$buildType build.")
            }

            changedModuleList = moduleList
            isMock = false
        }

        Util.printLog("okcheck: lint$flavor$buildType")
        if (extension.destination != project.buildDir) {
            def lintTask = project.tasks.findByName("lint$flavor$buildType")
            if (lintTask == null) {
                project.tasks.whenTaskAdded { task ->
                    if (task.name == "lint$flavor$buildType") {
                        task.doLast {
                            moveLintReport(project, extension)
                        }
                    }
                }
            } else {
                project.tasks.findByName("lint$flavor$buildType").doLast {
                    moveLintReport(project, extension)
                }
            }

        }
    }

    static def moveLintReport(Project project, OkCheckExtension extension) {
        File originFile = new File(project.buildDir, "reports/lint-results.html")
        if (originFile.exists()) {
            File targetFile = DestinationUtil.getHtmlDest(project, extension.destination, "lint")
            if (!targetFile.getParentFile().exists()) targetFile.getParentFile() mkdirs()
            FileUtils.copyFile(originFile, targetFile)
            Util.printLog("OkCheck: Copy ${originFile.path} to ${targetFile.path}.")
        }
    }

    static def addMockTask(Project project, String flavor, String buildType) {
        project.task(OkCheckPlugin.TASK_NAME + "$flavor$buildType", type: OkCheckTask, overwrite: true) {
            setGroup("verification")
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Run check only for changed files for all variants")
            } else {
                setDescription("Run check only for changed files for $flavor$buildType build.")
            }
            changedModuleList = new ArrayList<>()
            isMock = true
        }
    }
}
