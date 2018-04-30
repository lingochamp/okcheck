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

class DestinationUtil {

    static File getHtmlDest(Project project, File base, String type) {
        return getFileDest(project, base, type, "html")
    }

    static File getXmlDest(Project project, File base, String type) {
        return getFileDest(project, base, type, "xml")
    }

    static File getFileDest(Project project, File base, String type, String extension) {
        final String projectBuildPath = project.buildDir.absolutePath
        final String basePath = base.getAbsolutePath()

        if (basePath.startsWith(projectBuildPath)) {
            // already on the project path
            return new File(base, "reports/${type}.$extension")
        } else {
            // not on the project path
            return new File(base, "reports/${project.name}/${type}.$extension")
        }
    }

    static File getDirDest(Project project, File base, String type) {
        final String projectBuildPath = project.buildDir.absolutePath
        final String basePath = base.getAbsolutePath()

        if (basePath.startsWith(projectBuildPath)) {
            // already on the project path
            return new File(base, "reports/${type}/")
        } else {
            // not on the project path
            return new File(base, "reports/${project.name}/${type}/")
        }
    }
}
