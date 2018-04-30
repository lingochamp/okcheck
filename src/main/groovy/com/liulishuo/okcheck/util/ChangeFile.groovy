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

import groovy.io.FileType
import org.gradle.api.Project

class ChangeFile {
    public final String backupPath
    private final String currentBranchName = GitUtil.currentBranchName()
    private final String currentCommitId = GitUtil.currentCommitId()
    private final String projectName
    private final Project project

    ChangeFile(Project project) {
        this.projectName = project.name
        this.project = project
        backupPath = backupBranchCommitIdFilePath(currentBranchName)
    }

    List<String> getChangeFilePathList() {
        List<String> noCommitFilePathList = GitUtil.noCommitFilePathList()
        String lastExecCommitId = getLastExecCommitId()
        if (lastExecCommitId == currentCommitId) return noCommitFilePathList

        List<String> changeFileList = noCommitFilePathList
        changeFileList.addAll(GitUtil.diffFileToNowList(lastExecCommitId))
        return changeFileList
    }

    def refreshLastExecCommitId() {
        File backupFile = new File(backupPath)
        if (!backupFile.getParentFile().exists()) {
            backupFile.getParentFile().mkdirs()
        }

        if (!backupFile.exists()) {
            backupFile.createNewFile()
        }

        Util.printLog("OkCheck: Refresh $currentCommitId to $backupPath")
        backupFile.write(currentCommitId)
    }
    private String getLastExecCommitId() {
        File backupFile = new File(backupPath)
        if (backupFile.exists()) {
            return backupFile.readLines().get(0)
        } else {
            return GitUtil.commitIdFromDevelop(currentBranchName)
        }
    }

    String maintain() {
        String info = "maintain:"
        final List<String> branchNames = GitUtil.listAllBranches()
        final List<String> allBranchCommitIdPaths = new ArrayList<>()
        branchNames.forEach {
            allBranchCommitIdPaths.add(backupBranchCommitIdFilePath(it))
        }

        final commitBackupFile = new File(getCommitIdBackupPath())
        if (!commitBackupFile.exists()) {
            info += "no invalid backup found."
            return info
        }

        // assemble all no exist backups
        final List<String> invalidBackups = new ArrayList<>()
        commitBackupFile.eachFileRecurse(FileType.FILES) { file ->
            if (!allBranchCommitIdPaths.contains(file.absolutePath)) {
                invalidBackups.add(file.absolutePath)
            }
        }

        // delete all no exist backup
        invalidBackups.forEach {
            info += "\n         Delete overdue backup: $it"
            new File(it).delete()
        }
        invalidBackups.clear()

        // assemble all empty dir
        commitBackupFile.eachFileRecurse(FileType.DIRECTORIES) { dir ->
            if (dir.list().length == 0) invalidBackups.add(dir.absolutePath)
        }

        // delete all empty dir
        invalidBackups.forEach {
            File dir = new File(it)
            if (dir.exists()) {
                info += "\n         Delete Empty dir: $it"
                dir.delete()
            }
        }

        return info
    }

    String backupBranchCommitIdFilePath(String branchName) {
        return "${getCommitIdBackupPath()}/$branchName"
    }

    private String commitIdBackupPath = null

    String getCommitIdBackupPath() {
        if (commitIdBackupPath == null) commitIdBackupPath = "${okcheckHomePath(project)}/$projectName/commit-id"
        return commitIdBackupPath
    }

    static String okcheckHomePath(Project project) {
        File firstCandidate = new File(project.rootProject.absoluteProjectPath(".okcheck"))
        if (firstCandidate.exists()) return firstCandidate.absolutePath

        return "${System.getProperty("user.home")}/.okcheck"
    }
}
