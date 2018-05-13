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

package com.liulishuo.okcheck.util

import org.gradle.api.Project

class BuildConfig {
    final static String buildDirName = "okcheck-build"
    final static String moduleListFilename = "changed-module-name.txt"
    final static String passedModuleFilename = "passed-module-name.txt"

    static def saveChangedModuleList(Project project, List<String> changedModuleList) {
        File changeModuleSaveFile = getChangeModuleSaveFile(project)
        File parentDir = changeModuleSaveFile.getParentFile()

        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        if (changeModuleSaveFile.exists()) {
            changeModuleSaveFile.delete()
        }

        changeModuleSaveFile.createNewFile()
        FileWriter writer = new FileWriter(changeModuleSaveFile);
        try {
            changedModuleList.forEach {
                writer.write(it + "\n")
            }
        } finally {
            writer.flush()
            writer.close()
        }
    }

    static def setupPassedModuleFile(Project project) {
        File passedModuleFile = getPassedModuleFile(project)

        File parentDir = passedModuleFile.getParentFile()
        if (!parentDir) parentDir.mkdirs()

        if (passedModuleFile.exists()) passedModuleFile.delete()
    }

    static def addToPassedModuleFile(Project project) {
        File passedModuleFile = getPassedModuleFile(project.rootProject)
        if (!passedModuleFile.exists()) {
            File parentDir = passedModuleFile.getParentFile()
            if (!parentDir.exists()) parentDir.mkdirs()
            Util.printLog("Create passed module file: " + passedModuleFile.absolutePath)
            passedModuleFile.createNewFile()
        }

        passedModuleFile.append(project.name + "\n")
    }

    static boolean isAllModulePassed(Project project, List<String> changedModuleList) {
        File passedModuleFile = getPassedModuleFile(project.rootProject)
        if (!passedModuleFile.exists()) return false

        List<String> passedModule = passedModuleFile.readLines()

        List<String> leftModuleList = changedModuleList.clone()
        leftModuleList.removeAll(passedModule)

        return leftModuleList.isEmpty()
    }

    static List<String> getChangedModuleList(Project project) {
        final changeModuleSaveFile = getChangeModuleSaveFile(project.rootProject)
        return changeModuleSaveFile.readLines()
    }

    static File getChangeModuleSaveFile(Project project) {
        return new File(new File(project.getBuildDir(), buildDirName), moduleListFilename)
    }

    static File getPassedModuleFile(Project project) {
        return new File(new File(project.getBuildDir(), buildDirName), passedModuleFilename)
    }
}
