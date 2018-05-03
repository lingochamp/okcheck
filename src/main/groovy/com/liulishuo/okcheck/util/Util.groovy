/*
 * Copyright (c) 2018 LingoChamp Inc.
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

class Util {
    static boolean hasAndroidPlugin(Project project) {
        return project.plugins.hasPlugin("com.android.application")
    }

    static boolean hasLibraryPlugin(Project project) {
        return project.plugins.hasPlugin("com.android.library")
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

    static String getBuildInTaskName(String projectName, String hostTaskName, String taskName, String flavor,
                                     String buildType, String firstFlavor, String taskNameSuffix = "") {
        String name
        if (flavor.isEmpty() && buildType.isEmpty()) {
            // the empty lint is always exist
            name = "$taskName${taskNameSuffix.capitalize()}"
        } else if (flavor.isEmpty() && firstFlavor != null) {
            // the non empty build-type with empty flavor will not exist
            name = "$taskName${firstFlavor.capitalize()}${buildType.capitalize()}${taskNameSuffix.capitalize()}"
            printLog("There is define flavor(s) on $projectName, so on the $hostTaskName we have to add $name as dependencies task")
        } else {
            name = "$taskName${flavor.capitalize()}${buildType.capitalize()}${taskNameSuffix.capitalize()}"
        }

        return name
    }
}
