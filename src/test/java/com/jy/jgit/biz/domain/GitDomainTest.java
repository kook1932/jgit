package com.jy.jgit.biz.domain;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class GitDomainTest {

	@DisplayName("Git Push Test")
	@Test
	void gitPushTest() {
		GitDomain git = GitDomain.builder()
				.username("kook1932")
				.dirPath("C:\\Projects\\jgit")
				.build();

		git.push("origin", "master");
	}

	@DisplayName("List local commits in a repository")
	@Test
	void getLocalCommitList() {
		GitDomain git = GitDomain.builder()
//				.dirPath("C:\\Projects\\jgit")
				.dirPath("C:\\Users\\jeongyong.han\\project\\jgit")
				.build();

		List<RevCommit> commitList = git.getLocalCommitList("master");
		commitList.forEach(c -> System.out.println("commit : " + c.getFullMessage()));
		System.out.println("localCommit count = " + commitList.size());
	}

	@DisplayName("List local commits in a repository Without Git init")
	@Test
	void getLocalCommitListV2() {
		GitDomain git = GitDomain.builder()
				.dirPath("C:\\Users\\jeongyong.han\\project\\jgit")
				.build();

		List<RevCommit> commitList = git.getLocalCommitList("dev");
		commitList.forEach(c -> System.out.println("commit : " + c + ", message : " + c.getFullMessage()));
		System.out.println("localCommit count = " + commitList.size());
	}

	@DisplayName("List Remotes commits in a repository")
	@Test
	void getRemotesCommitList() {
		GitDomain git = GitDomain.builder()
//				.dirPath("C:\\Projects\\jgit")
				.dirPath("C:\\Users\\jeongyong.han\\project\\jgit")
				.build();

		List<RevCommit> commitList = git.getRemotesCommitList("origin/master");
		commitList.forEach(c -> System.out.println("commit : " + c.getFullMessage()));
		System.out.println("remotesCommit count = " + commitList.size());
	}

	@DisplayName("git checkout -b cherry-branch origin/master")
	@Test
	void checkoutRemoteBranchInNewBranchTest() {
		GitDomain git = GitDomain.builder()
				.dirPath("C:\\Users\\jeongyong.han\\project\\jgit")
				.build();

		git.checkoutRemoteBranchInNewBranch("origin/master", "cherry-branch");
	}

	@DisplayName("show no push Commits")
	@Test
	void reservationTest() {
		GitDomain git = GitDomain.builder()
//				.dirPath("C:\\Projects\\jgit")
				.dirPath("D:\\side\\jgit")
				.build();

//		git.reservation(LocalDateTime.now(), "master", "origin/master");
		git.reservation(LocalDateTime.of(2023, 5, 5, 10, 24), "master", "origin/master");
	}

	@DisplayName("마지막 커밋 조회")
	@Test
	public void getCountNotCommit() throws Exception {
	    // given
		GitDomain git = GitDomain.builder()
				.dirPath("D:\\side\\jgit")
				.build();

		// when
		int countNotCommit = git.getCountNotCommit();

		// then
		System.out.println("countNotCommit = " + countNotCommit);
	}

	@DisplayName("리모트에 푸시안된 커밋 개수")
	@Test
	public void getNoPushCommitsCountTest() throws Exception {
	    //given
		GitDomain git = GitDomain.builder()
				.dirPath("D:\\side\\jgit")
				.build();

	    //when
		int count = git.getNoPushCommitsCount("master", "origin/master");

		//then
		System.out.println("count = " + count);
	}

}