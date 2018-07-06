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

import com.android.annotations.Nullable
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

    @Nullable
    List<String> getChangeFilePathList() {
        List<String> noCommitFilePathList = GitUtil.noCommitFilePathList()
        String latestSuccessId = getLatestSuccessId()
        if (latestSuccessId == currentCommitId) return noCommitFilePathList

        if (latestSuccessId == null) return null

        List<String> changeFileList = noCommitFilePathList
        changeFileList.addAll(GitUtil.diffFileToNowList(latestSuccessId))
        return changeFileList
    }

    def refreshLastExecCommitId() {
        File backupFile = new File(backupPath)
        if (!backupFile.getParentFile().exists()) {
            backupFile.getParentFile().mkdirs()
        }

        final List<String> backupCommitIdList
        if (backupFile.exists()) {
            backupCommitIdList = backupFile.readLines()
            backupFile.delete()
        } else {
            backupCommitIdList = new ArrayList<>()
        }

        backupFile.createNewFile()

        if (backupCommitIdList.size() >= 20) {
            Util.printLog("count of backupCommitIdList: ${backupCommitIdList.size()} which will trun to 20")
            int needRemoveSize = backupCommitIdList.size() - 20 + 1
            while (needRemoveSize > 0) {
                needRemoveSize--
                backupCommitIdList.remove(backupCommitIdList.size() - 1)
            }
        }

        backupCommitIdList.add(0, currentCommitId)
        boolean isFirstLine = true
        for (String commitId : backupCommitIdList) {
            if (isFirstLine) {
                backupFile.append(commitId)
                isFirstLine = false
            } else {
                backupFile.append("\n" + commitId)
            }
        }

        Util.printLog("Save commit <${backupCommitIdList.toArray()}> to $backupPath")
    }

    @Nullable
    private String getLatestSuccessId() {
        String candidateId = null
        String branchName = null

        while (true) {
            File backupFile = new File(backupPath)
            if (backupFile.exists()) {
                candidateId = backupFile.readLines().get(0)
            }
            List<String> allBeforeCommitIds = GitUtil.getAllBeforeCommitIds()
            if (candidateId != null && allBeforeCommitIds.contains(candidateId)) {
                branchName = currentBranchName
                break
            }

            Util.printLog("Can't find the $currentBranchName file, so try others")
            candidateId = null
            def dir = new File(getCommitIdBackupPath())

            int leastCount = -1
            dir.eachFileRecurse(FileType.FILES) { file ->
                List<String> ids = file.readLines()

                for (String id : ids) {
                    id = id.trim().replaceAll("[\r\n]+", "")
                    if (id == null || id.length() <= 0) continue

                    if (allBeforeCommitIds.contains(id)) {
                        int count = GitUtil.farToCommit(id, currentCommitId)
                        Util.printLog("far to $count of $id")
                        if (leastCount == -1 || leastCount > count) {
                            leastCount = count
                            candidateId = id
                            branchName = (file.absolutePath - dir.absolutePath).substring(1)
                        }
                    }
                }
            }

            break
        }


        if (branchName != null) {
            Util.printLog("Found latest success check the commit id[$candidateId] which was ran on the branch[$branchName]")
        }
        return candidateId
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

                // special case
                if (file.name == "HEAD") {
                    // remain HEAD
                    Util.printLog("remain HEAD this special branch")
                } else {
                    invalidBackups.add(file.absolutePath)
                }
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
