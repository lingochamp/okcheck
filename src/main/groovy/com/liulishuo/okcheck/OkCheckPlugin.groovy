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
import com.liulishuo.okcheck.util.GitUtil
import com.liulishuo.okcheck.util.IncrementFilesHelper
import com.liulishuo.okcheck.util.Util
import org.gradle.api.Plugin
import org.gradle.api.Project

class OkCheckPlugin implements Plugin<Project> {

    static final TASK_NAME = "okcheck"

    private OkCheckExtension okCheckExtension

    @Override
    void apply(Project project) {
        if (isRequireOkCheck(project)) Util.enableLog()

        okCheckExtension = project.extensions.create("okcheck", OkCheckExtension, project)

        // clean okcheck diff
        if (project == project.rootProject) CleanOkCheckDiffTask.addTask(project)

        // lint, ktlint, checkstyle, unitTest, pmd, findbugs
        if (project != project.rootProject || project.rootProject.subprojects.size() <= 0) {
            project.afterEvaluate {

                okCheckExtension = configureIncrementTask(project,okCheckExtension)

                OkLint.inspectLint(project, okCheckExtension.lint)
                if (okCheckExtension.checkStyle.enabled) OkCheckStyleTask.addTask(project, okCheckExtension.checkStyle)
                if (okCheckExtension.pmd.enabled) OkPmdTask.addTask(project, okCheckExtension.pmd)
                if (okCheckExtension.findbugs.enabled) OkFindbugsTask.addTask(project, okCheckExtension.findbugs)
                if (okCheckExtension.ktlint.enabled) OkKtlintTask.addTask(project, okCheckExtension.ktlint)
                if (okCheckExtension.coverageReport.isEnabled()) OkCoverageReport.addTask(project, okCheckExtension.coverageReport)
                // unit-test
            }
        }

        // diff okcheck
        String branchName = GitUtil.currentBranchName()
        if (branchName == null || branchName.length() <= 0) {
            Util.printLog("This isn't the valid okcheck env, okcheck must running on the git repo!")
            return
        }

        if (project == project.rootProject) {
            // root project
            setupOkCheckDiff(project)
            if (project.subprojects.size() <= 0) {
                distributeOkCheckTask(project)
            }
        } else {
            // changed module list
            distributeOkCheckTask(project)
        }
    }

    private static OkCheckExtension configureIncrementTask(Project project, OkCheckExtension okCheckExtension) {
        List<String> changeFiles = IncrementFilesHelper.instance.getModuleChangeFiles(project.name)
        if (changeFiles.isEmpty()) return okCheckExtension

        boolean isEnableCheckStyle = false
        boolean isEnableFindBugs = false
        boolean isEnablePMD = false
        boolean isEnableKtLint = false
        for (String fileName : changeFiles) {
            if (fileName.contains(".java")) {
                if (okCheckExtension.checkStyle.enabled) isEnableCheckStyle = true
                if (okCheckExtension.findbugs.enabled) isEnableFindBugs = true
                if (okCheckExtension.pmd.enabled) isEnablePMD = true
            } else if (fileName.contains(".kt")) {
                if (okCheckExtension.ktlint.enabled) isEnableKtLint = true
            }
        }

        okCheckExtension.checkStyle.enabled = isEnableCheckStyle
        okCheckExtension.findbugs.enabled = isEnableFindBugs
        okCheckExtension.pmd.enabled = isEnablePMD
        okCheckExtension.ktlint.enabled = isEnableKtLint
        return okCheckExtension
    }

    private void distributeOkCheckTask(Project project) {
        final List<String> changedModuleList = BuildConfig.getChangedModuleList(project)
        project.afterEvaluate {
            if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) {
                Util.printLog("Pass ${project.name} directly because it isn't android/library project.")
                BuildConfig.addToPassedModuleFile(project)
                return
            }

            boolean isChangedModule = changedModuleList.contains(project.name)
            if (isChangedModule) {
                Util.printLog("Enable check for ${project.name} because of file changed on it")
                OkCheckTask.addValidTask(project, changedModuleList, okCheckExtension)
            } else {
                Util.printLog("NO CHANGED CODE FOUND FOR ${project.name}")
                OkCheckTask.addMockTask(project)
            }
        }
    }

    private static void setupOkCheckDiff(Project project) {
        if (project != project.rootProject) throw new IllegalAccessException("only can invoke by the root project!")

        boolean isRequireOkCheck = isRequireOkCheck(project)
        final List<String> changedModuleList = new ArrayList<>()

        boolean ignoreDiff = isIgnoreDiff(project)
        if (ignoreDiff) {
            final List<String> pointTaskNameList = getPointTargetModelNameList(project)
            if (pointTaskNameList.size() > 0) {
                Util.printLog("ignore okcheck diff means target module(${pointTaskNameList} is free to okcheck!")
                changedModuleList.addAll(pointTaskNameList)
            } else {
                Util.printLog("ignore okcheck diff means every module is free to okcheck!")
                changedModuleList.addAll(ChangeModule.getAllModuleList(project))
            }
        } else if (isFirstBlood(project)) {
            Util.printLog("First blood means every module is free to okcheck!")
            changedModuleList.addAll(ChangeModule.getAllModuleList(project))
        } else {
            ChangeFile changeFile = new ChangeFile(project.rootProject)

            List<String> changeFilePathList = changeFile.getChangeFilePathList()
            if (changeFilePathList == null) {
                Util.printLog("There isn't success okcheck on .okcheck folder, so every module is free to okcheck!")
                changedModuleList.addAll(ChangeModule.getAllModuleList(project))
            } else {
                if (isRequireOkCheck) {
                    Util.printLog("COMMIT ID BACKUP PATH: ${changeFile.backupPath}")

                    Util.printLog("CHANGE FLIES:")
                    changeFilePathList.forEach {
                        Util.printLog("       $it")
                    }
                }
                List<String> changedCodeFilePathList = new ArrayList<>()
                changeFilePathList.forEach {
                    if (it.endsWith(".java") || it.endsWith(".groovy") || it.endsWith(".kt") || it.endsWith(".xml")) {
                        changedCodeFilePathList.add(it)
                    }
                }

                if (changedCodeFilePathList.isEmpty()) {
                    Util.printLog("NO CHANGED CODE FILE!")
                } else {
                    changedModuleList.addAll(ChangeModule.getChangedModuleList(project, changedCodeFilePathList))
                    if (isRequireOkCheck) {
                        Util.printLog("CHANGE MODULES:")
                        changedModuleList.forEach {
                            Util.printLog("       $it")
                        }
                    }
                }
            }
        }


        BuildConfig.saveChangedModuleList(project, changedModuleList)
        BuildConfig.setupPassedModuleFile(project)
    }

    private static boolean isRequireOkCheck(Project project) {
        return Util.isCommandContainTask(project, TASK_NAME)
    }

    private static List<String> getPointTargetModelNameList(Project project) {
        def taskNames = project.gradle.startParameter.taskNames
        List<String> pointTaskNameList = new ArrayList<>()

        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)
            if (name.endsWith(TASK_NAME) && name.contains(":")) {
                String[] splitNames = name.split(":")
                String projectName = splitNames[splitNames.size() - 1]
                project.rootProject.subprojects {
                    if (projectName == it.name) pointTaskNameList.add(projectName)
                }
            } else if (name == TASK_NAME) {
                return new ArrayList<String>()
            }
        }

        return pointTaskNameList
    }

    private static boolean isIgnoreDiff(Project project) {
        return project.hasProperty("ignoreOkcheckDiff")
    }

    private static boolean isFirstBlood(Project project) {
        String projectHomePath = "${ChangeFile.okcheckHomePath(project)}/${project.rootProject.name}"
        // not exist means first time to invoke okcheck(maybe just clean time).
        return !new File(projectHomePath).exists()
    }
}
