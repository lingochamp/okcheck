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

class ChangeFile {
    public final String backupPath
    private final String currentBranchName = GitUtil.currentBranchName()
    private final String currentCommitId = GitUtil.currentCommitId()

    ChangeFile(String projectName) {
        backupPath = "${System.getProperty("user.home")}/.okcheck/$projectName/$currentBranchName"
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

        if (!backupFile.exists()){
            backupFile.createNewFile()
        }

        println "OkCheck: Refresh $currentCommitId to $backupPath"
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

}
