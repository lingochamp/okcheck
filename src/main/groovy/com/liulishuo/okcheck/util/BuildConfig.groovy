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
    final static String moduleListFileName = "changed-module-name.txt"

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

    static List<String> getChangedModuleList(Project project) {
        final changeModuleSaveFile = getChangeModuleSaveFile(project.rootProject)
        return changeModuleSaveFile.readLines()
    }

    static File getChangeModuleSaveFile(Project project) {
        return new File(new File(project.getBuildDir(), buildDirName), moduleListFileName)
    }
}
