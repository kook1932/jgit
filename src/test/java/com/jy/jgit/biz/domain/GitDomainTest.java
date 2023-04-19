package com.jy.jgit.biz.domain;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitDomainTest {

	@DisplayName("Git Push Test")
	@Test
	void gitPushTest() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Projects\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		git.push();
	}

	@DisplayName("List all commits in a repository")
	@Test
	void getList() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Projects\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		List<RevCommit> commitList = git.getCommitList();
		commitList.forEach(c -> System.out.println("commit : " + c));
	}

}