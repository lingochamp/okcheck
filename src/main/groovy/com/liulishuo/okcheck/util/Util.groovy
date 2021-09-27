/*
 * Copyright (c) 2018 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
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
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import java.util.regex.Matcher
import java.util.regex.Pattern

class Util {
    static boolean hasAndroidPlugin(Project project) {
        return project.plugins.hasPlugin("com.android.application")
    }

    static boolean hasLibraryPlugin(Project project) {
        return project.plugins.hasPlugin("com.android.library")
    }

    static boolean hasJacocoPlugin(Project project) {
        return project.plugins.hasPlugin("jacoco")
    }


    private static boolean isEnableLog = false

    static void enableLog() {
        isEnableLog = true
    }

    static void printLog(String msg) {
        if (!isEnableLog) return
        println("OkCheck: $msg")
    }

    static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false

        final int length = searchStr.length()
        if (length == 0) return true

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true
        }
        return false
    }

    static boolean isCommandContainTask(Project project, String taskName) {
        def taskNames = project.gradle.startParameter.taskNames

        boolean contain = false
        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)
            if (containsIgnoreCase(name, taskName)) {
                contain = true
                break
            }
        }
        return contain
    }

    static String getUnitTestTaskName(String projectName, String hostTaskName, String flavor,
                                      String buildType, String firstFlavor) {

        if (buildType.isEmpty() && flavor.isEmpty()) {
            return "test"
        } else {
            return getBuildInTaskName(projectName, hostTaskName, "test", flavor, buildType, firstFlavor, "UnitTest")
        }
    }

    static String getBuildInTaskName(String projectName, String hostTaskName, String taskName, String flavor,
                                     String buildType, String firstFlavor, String taskNameSuffix = "") {
        String name
        if (flavor.isEmpty() && buildType.isEmpty()) {
            // the task with empty flavor & build type is always exist
            name = "$taskName${taskNameSuffix.capitalize()}"
        } else if (flavor.isEmpty() && !firstFlavor.isEmpty()) {
            // there is not exist non empty build-type with empty flavor when there are flavors defined
            name = "$taskName${firstFlavor.capitalize()}${buildType.capitalize()}${taskNameSuffix.capitalize()}"
            printLog("There is define flavor(s) on $projectName, so on the $hostTaskName we have to add $name as dependencies task")
        } else {
            name = "$taskName${flavor.capitalize()}${buildType.capitalize()}${taskNameSuffix.capitalize()}"
        }

        return name
    }

    static addTaskWithVariants(Project project, addTask) {
        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }

        String firstFlavor = ""
        if (productFlavors.size() > 0) firstFlavor = productFlavors.get(0)

        addTask("", "", firstFlavor)

        buildTypes.each { buildType ->
            addTask("", "$buildType", firstFlavor)
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addTask("$flavor", "$buildType", firstFlavor)
                }
            }
        }
    }

    static def getExclude() {
        return [
                '**/gen/**',
                '**/test/**',
                '**/proto/*.java',
                '**/protobuf/*.java',
                '**/com/google/**/*.java',
                "android/*",
                "androidx/*",
                "com/android/*"
        ]
    }

    static getIncludeByType(Project project,InputType type) {
        List<String> moduleChangeFiles = IncrementFilesHelper.instance.getModuleChangeFiles(project.name)
        if (moduleChangeFiles.isEmpty()) {
            return getNormalInclude(type)
        } else {
            return getIncrementInclude(moduleChangeFiles,type)
        }
    }

    private static String[] getNormalInclude(InputType type) {
        String[] include = null
        switch (type) {
            case InputType.KT_LINT:
                include = ["**/*.kt"]
                break
            case InputType.FIND_BUGS:
            case InputType.PMD:
            case InputType.CHECK_STYLE:
                include = ["**/*.java"]
                break
            case InputType.LINT:
                include = [
                        "**/*.kt",
                        "**/*.java",
                        "**/*.groovy",
                        "**/*.xml",
                        "**/*.gradle"
                ]
                break
        }

        return include
    }

    private static String[] getIncrementInclude(List<String> changeFiles ,InputType type) {
        def include = []
        for (String fileName in changeFiles) {
            switch (type) {
                case InputType.KT_LINT:
                    if (fileName.endsWith(".kt")) {
                        include.add(getFilterPath(fileName,type))
                    }
                    break
                case InputType.FIND_BUGS:
                case InputType.PMD:
                case InputType.CHECK_STYLE:
                    if (fileName.endsWith(".java")) {
                        final name = getFilterPath(fileName,type)
                        if (name != null && name.length() > 0) {
                            include.add(name)
                        }
                    }
                    break
                default:
                    include.add(fileName)
            }
        }

        return include as String[]
    }

    private static String getFilterPath(String fileName,InputType type) {
        String regex = ""
        switch (type) {
            case InputType.FIND_BUGS:
            case InputType.PMD:
            case InputType.CHECK_STYLE:
                regex = "[a-zA-Z_0-9]*\\.java"
                break
            case InputType.KT_LINT:
                regex = "[a-zA-Z_0-9]*\\.kt"

        }
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher(fileName)
        while (matcher.find()) {
            return "**/" +  matcher.group()
        }
    }

    static def getAllInputs(Project project) {
        def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
        inputFiles += project.fileTree(dir: "src", include: "**/*.java")
        inputFiles += project.fileTree(dir: "src", include: "**/*.groovy")
        inputFiles += project.fileTree(dir: "src", include: "**/*.xml")
        return inputFiles
    }

    static def getInputsByType(Project project, InputType type) {
        FileTree inputFiles = project.fileTree(dir: 'src/main/java')
        List<String> moduleChangeFiles = IncrementFilesHelper.instance.getModuleChangeFiles(project.name)
        if (moduleChangeFiles.isEmpty()) {
            inputFiles = getFullInputs(project,type, inputFiles)
        } else {
            getIncrementInputFiles(moduleChangeFiles,inputFiles, type)
        }
        inputFiles.matching {
            exclude '**/gen/**', '**/test/**'
            exclude '**/proto/*.java'
            exclude '**/protobuf/*.java'
            exclude '**/com/google/**/*.java'
            exclude "android/*"
            exclude "androidx/*"
            exclude "com/android/*"
        }

        return inputFiles
    }

    private static void getIncrementInputFiles(List<String> changeFiles,ConfigurableFileTree inputFiles, InputType type) {
        for (String fileName in changeFiles) {
            switch (type) {
                case InputType.KT_LINT:
                    if (fileName.endsWith(".kt")) {
                        inputFiles.include "$fileName"
                    }
                    break
                case InputType.FIND_BUGS:
                case InputType.PMD:
                case InputType.CHECK_STYLE:
                    if (fileName.endsWith(".java")) {
                        inputFiles.include "$fileName"
                    }
                    break
                default:
                    inputFiles.include "$fileName"
            }
        }
    }
    private static FileTree getFullInputs(Project project,InputType type, FileTree inputFiles) {
        switch (type) {
            case InputType.KT_LINT:
                inputFiles.matching { include: "**/*.kt" }
                break
            case InputType.FIND_BUGS:
            case InputType.PMD:
            case InputType.CHECK_STYLE:
                inputFiles = inputFiles.matching { include: "**/*.java" }
                break
            case InputType.LINT:
                inputFiles = getAllInputs(project)
                break
        }
        inputFiles
    }

    enum InputType {
        LINT,
        PMD,
        KT_LINT,
        FIND_BUGS,
        CHECK_STYLE
    }
}
