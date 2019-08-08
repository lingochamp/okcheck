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

import com.liulishuo.okcheck.util.IncrementFilesHelper
import com.liulishuo.okcheck.util.Util
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.JavaExec

class OkKtlintTask extends JavaExec {

    OkKtlintTask() {
        doFirst {
            Util.printLog("${project.name} runing OkKtlintTask")
        }
    }

    static String NAME = "okKtlint"

    static void addTask(Project project, OkCheckExtension.KtLintOptions options) {
        project.configure(project) {
            project.configurations {
                ktlint
            }

            dependencies {
                ktlint "com.github.shyiko:ktlint:${options.version}"
            }
        }

        FileTree inputFiles = null
        if (IncrementFilesHelper.instance.incrementFiles.isEmpty()) {
            inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
        } else {
            inputFiles = project.fileTree(dir: 'src/main/java')
            for (String fileName in IncrementFilesHelper.instance.incrementFiles) {
                if (fileName.endsWith(".kt")) {
                    inputFiles.include "$fileName"
                }
            }
        }

        if (options.exclude.size() > 0) inputFiles.exclude(options.exclude)
        def outputFile = options.xmlFile

        project.task(NAME, type: OkKtlintTask) {
            inputs.files(inputFiles)
            outputs.file(outputFile)

            group = "verification"
            description = "Runs ktlint."
            main = "com.github.shyiko.ktlint.Main"
            classpath = project.configurations.ktlint
            args = [
                    "--reporter=plain",
                    "--reporter=checkstyle,output=${outputFile}",
                    "src/**/*.kt"
            ]
        }

        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}
