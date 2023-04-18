package com.jy.jgit.biz.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitDomainTest {

    @DisplayName("Git Push Test")
    @Test
    void gitPushTest() {
        GitDomain git = GitDomain.builder()
                .username("kook1932")
                .userToken("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")
                .dirPath("C:\\Projects\\jgit")
                .repoName("jgit")
                .branchName("master")
                .build();

        git.push();

    }

}