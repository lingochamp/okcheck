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
import com.liulishuo.okcheck.util.DestinationUtil
import com.liulishuo.okcheck.util.Util
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class OkCheckTask extends DefaultTask {
    @Input
    List<String> changedModuleList

    @Input
    boolean isMock

    @TaskAction
    void setupOkcheck() {
        if (project == project.rootProject) {
            Util.printLog("Finish root okcheck task!")
        } else if (!isMock) {
            Util.printLog("Finish ${project.name} okcheck task!")
            BuildConfig.addToPassedModuleFile(project)

            if (BuildConfig.isAllModulePassed(project, changedModuleList)) {
                ChangeFile changeFile = new ChangeFile(project.rootProject)
                changeFile.refreshLastExecCommitId()
                Util.printLog("All check is passed and refreshed the commit to current one!")
                Util.printLog(changeFile.maintain())
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
        dependsTaskNames.add(OkLint.getTaskName(flavor, buildType))
        dependsTaskNames.add("test$flavor${buildType}UnitTest")
        if (extension.enableCheckstyle) dependsTaskNames.add(OkCheckStyleTask.NAME)
        if (extension.enablePmd) dependsTaskNames.add(OkPmdTask.NAME)
        if (extension.enableFindbugs) dependsTaskNames.add("${OkFindbugsTask.NAME}$flavor$buildType")
        if (extension.enableKtlint) dependsTaskNames.add(OkKtlintTask.NAME)

        def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
        inputFiles += project.fileTree(dir: "src", include: "**/*.java")
        inputFiles += project.fileTree(dir: "src", include: "**/*.groovy")
        inputFiles += project.fileTree(dir: "src", include: "**/*.xml")

        project.task(OkCheckPlugin.TASK_NAME + "$flavor$buildType", type: OkCheckTask, overwrite: true) {
            inputs.files(inputFiles)
            outputs.dir(project.buildDir)

            dependsOn dependsTaskNames
            setGroup("verification")
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Run check only for changed files for all variants")
            } else {
                setDescription("Run check only for changed files for $flavor$buildType build.")
            }

            changedModuleList = moduleList
            isMock = false

            if (extension.destination != project.buildDir) {
                doLast {
                    moveUnitTestReport(project, extension.destination)
                }
            }
        }
    }

    static def moveUnitTestReport(Project project, File destination) {
        File originDir = new File(project.buildDir, "reports/tests")
        if (originDir.exists()) {
            File targetDir = DestinationUtil.getDirDest(project, destination, "tests")
            if (!targetDir.getParentFile().exists()) targetDir.getParentFile().mkdirs()
            FileUtils.copyDirectory(originDir, targetDir)
            Util.printLog("Copy ${originDir.path} to ${targetDir.path}.")
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
