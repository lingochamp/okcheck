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

package com.liulishuo.okcheck

import com.liulishuo.okcheck.util.Util
import org.gradle.api.Project

class OkCheckExtension {
    boolean enableCheckstyle = true
    boolean enableFindbugs = true
    boolean enablePmd = true
    boolean enableKtlint = true
    File destination

    File checkStyleConfig = null
    File findBugsExcludeFilterConfig = null
    File pmdRuleSetConfig = null

    String[] exclude = []

    OkCheckExtension(Project project) {
        destination = project.rootProject.getBuildDir()
        if (project == project.rootProject) {
            project.afterEvaluate {
                if (checkStyleConfig != null) {
                    Util.printLog("Using the custom checkstyle config.")
                } else {
                    Util.printLog("Using the default checkstyle config.")
                }

                if (findBugsExcludeFilterConfig != null) {
                    Util.printLog("Using the custom findbugs exclude filter config.")
                } else {
                    Util.printLog("Using the default findbugs exclude filter config.")
                }

                if (pmdRuleSetConfig != null) {
                    Util.printLog("Using the custom pmd rule set config.")
                } else {
                    Util.printLog("Using the default pmd rule set config.")
                }
            }

        }
    }
}