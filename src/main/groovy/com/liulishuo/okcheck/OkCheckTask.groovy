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
        setDescription("check project only for changed files")
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
        if (project.tasks.findByName('lint') == null) {
            addMockTask(project)
            println("OkCheck: lint task can not be found, so it must not be android/library module, then we will not" +
                    " run any real check on this module ${project.name}")
            return
        }

        Set<String> dependsTaskNames = new HashSet<>()
        dependsTaskNames.add('lint')
        if (extension.enableCheckstyle) dependsTaskNames.add(OkCheckStyleTask.NAME)
        if (extension.enablePmd) dependsTaskNames.add(OkPmdTask.NAME)
        if (extension.enableFindbugs) dependsTaskNames.add(OkFindbugsTask.NAME)
        if (extension.enableKtlint) dependsTaskNames.add(OkKtlintTask.NAME)

        project.task(OkCheckPlugin.TASK_NAME, type: OkCheckTask, overwrite: true) {
            dependsOn dependsTaskNames
            changedModuleList = moduleList
            isMock = false
        }

        if (extension.destination != project.buildDir) {
            project.tasks.findByName('lint').doLast {
                File originFile = new File(project.buildDir, "reports/lint-results.html")
                if (originFile.exists()) {
                    File targetFile = DestinationUtil.getHtmlDest(project, extension.destination, "lint")
                    if (!targetFile.getParentFile().exists()) targetFile.getParentFile() mkdirs()
                    FileUtils.copyFile(originFile, targetFile)
                    println("OkCheck: Copy ${originFile.path} to ${targetFile.path}.")
                }
            }
        }
    }

    static def addMockTask(Project project) {
        project.task(OkCheckPlugin.TASK_NAME, type: OkCheckTask, overwrite: true) {
            changedModuleList = new ArrayList<>()
            isMock = true
        }
    }
}
