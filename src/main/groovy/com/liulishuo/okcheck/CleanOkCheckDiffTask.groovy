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

import com.liulishuo.okcheck.util.ChangeFile
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class CleanOkCheckDiffTask extends DefaultTask {

    @TaskAction
    void clean() {
        File homePath = ChangeFile.okcheckHomePath()
        println("OkCheck: delete ${homePath.path}")
        FileUtils.deleteDirectory(new File(ChangeFile.okcheckHomePath()))
    }

    static void addTask(Project project) {
        project.task("cleanOkcheckDiff", type: CleanOkCheckDiffTask)
    }
}
