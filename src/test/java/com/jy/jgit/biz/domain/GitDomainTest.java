package com.jy.jgit.biz.domain;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

	@DisplayName("List local commits in a repository")
	@Test
	void getLocalCommitList() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Projects\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		List<RevCommit> commitList = git.getLocalCommitList();
		commitList.forEach(c -> System.out.println("commit : " + c));
		System.out.println("localCommit count = " + commitList.size());
	}

	@DisplayName("List Remotes commits in a repository")
	@Test
	void getRemotesCommitList() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Projects\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		List<RevCommit> commitList = git.getRemotesCommitList();
		commitList.forEach(c -> System.out.println("commit : " + c));
		System.out.println("remotesCommit count = " + commitList.size());
	}

	@DisplayName("git checkout -b cherry-branch origin/master")
	@Test
	void checkoutRemoteBranchInNewBranchTest() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Users\\jeongyong.han\\project\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		git.checkoutRemoteBranchInNewBranch();
	}

	@DisplayName("show no push Commits")
	@Test
	void reservationTest() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.userToken("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")
				.dirPath("C:\\Projects\\jgit")
				.repoName("origin")
				.branchName("master")
				.build();

		git.reservation();
	}

}