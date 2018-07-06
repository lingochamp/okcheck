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

class GitUtil {
    final static String developBranchName = 'develop'

    static String currentBranchName() {
        final String branchName = runCmd("git name-rev --name-only HEAD")
        if (branchName.contains('~')) {
            return branchName.split('~')[0]
        } else {
            return branchName
        }

    }

    static String commitIdFromDevelop(String branchName) {
        return runCmd("git merge-base $developBranchName $branchName")
    }

    static String currentCommitId() {
        return runCmd('git log --format="%H" -n 1')
    }

    static List<String> diffFileToNowList(String commitId) {
        return filterInvalidLine(runCmd("git diff --name-only --diff-filter=ACMRTUXB $commitId HEAD~0")
                .split('\n'))
    }

    static List<String> noCommitFilePathList() {
        final List<String> originList = filterInvalidLine(runCmd('git status -s').split('\n'))
        return assembleLastStringForEachLine(originList)
    }

    static List<String> listAllBranches() {
        final List<String> branchLines = filterInvalidLine(runCmd('git branch -a').split('\n'))
        return assembleLastStringForEachLine(branchLines)
    }

    static List<String> lastCommitIdList(int limitCount) {
        filterInvalidLine(runCmd("git log --pretty='%h' -n $limitCount").split('\n'))
    }

    static int farToCommit(String fromCommitId, String toCommitId) {
        return runCmd("git rev-list $fromCommitId...$toCommitId --count").toInteger()
    }

    static List<String> getAllBeforeCommitIds() {
        return filterInvalidLine(runCmd("git log --pretty=format:\"%H\"").split('\n'))
    }

    static List<String> assembleLastStringForEachLine(List<String> originList) {
        final List<String> lastStringList = new ArrayList<>();
        originList.forEach {
            String[] splitLine = it.split(" ")
            lastStringList.add(splitLine[splitLine.length - 1])
        }
        return lastStringList
    }

    static List<String> filterInvalidLine(String[] paths) {
        List<String> validLineList = new ArrayList<>()
        for (int i = 0; i < paths.size(); i++) {
            String path = paths[i].trim()
            if (paths != null && paths.length > 0) validLineList.add(path)
        }
        return validLineList
    }

    static String runCmd(String cmd) {
        return cmd.execute().text.trim().replaceAll("\"", "")
    }
}
