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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class OkCheckTask extends DefaultTask {
    OkCheckTask() {
        setDescription("check project only for changed files")
    }

    @TaskAction
    void setupOkcheck() {
        // ignore debug
        project.plugins.whenPluginAdded { plugin ->
            if ('com.android.build.gradle.LibraryPlugin' == plugin.class.name) {
                project.android.variantFilter {
                    if (it.buildType.name == 'debug') {
                        it.ignore = true
                    }
                }
            }
        }
    }
}
