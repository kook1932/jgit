package com.jy.jgit.biz.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor
public class GitDomain {

	private String username;
	private String userToken;
	private String dirPath;

	private CredentialsProvider credentialsProvider;

	@Builder
	public GitDomain(String username, String userToken, String dirPath) {
		this.username = username;
		this.userToken = userToken;
		this.dirPath = dirPath;

		// username, userToken 과 함께 생성할때만 CredentialsProvider 를 생성한다.
		if (username != null && !username.isEmpty() && userToken != null && !userToken.isEmpty()) {
			this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, userToken);
		}
	}

	// dirPath 에 존재하는 File 객체 Return
	public File getLocalRepoFile() {
		return new File(dirPath);
	}

	/**
	 * Git Push Method
	 * 	- 현재 커밋들을 remote/branchName 에 push 한다
	 * 	- push 하기 위해선 username, userToken, dirPath 으로 객체 생성이 필요함
 	 */
	public void push(String remote, String branchName) {
		try (Git git = Git.open(getLocalRepoFile())) {
			git.push()
					.setCredentialsProvider(credentialsProvider)
					.setRemote(remote)
					.setRefSpecs(new RefSpec(branchName))
					.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// List all commits in a repository
	public List<RevCommit> getLocalCommitList(String localBranchName) {
		List<RevCommit> list = new ArrayList<>();
		try (Git git = Git.open(getLocalRepoFile())) {
			Iterable<RevCommit> revCommits = git.log()
					.add(git.getRepository().resolve(localBranchName))
					.call();
			revCommits.iterator().forEachRemaining(list::add);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<RevCommit> getRemotesCommitList(String remoteBranchName) {
		List<RevCommit> list = new ArrayList<>();
		try (Git git = Git.open(getLocalRepoFile())) {
			Iterable<RevCommit> revCommits = git.log()
					.add(git.getRepository().resolve("remotes/" + remoteBranchName))
					.call();
			revCommits.iterator().forEachRemaining(list::add);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean isPossibleToPush(String localBranchName, String remoteBranchName) {
		return getLocalCommitList(localBranchName).size() - getRemotesCommitList(remoteBranchName).size() > 0;
	}

	public void checkoutRemoteBranchInNewBranch(String remoteBranchName, String newBranchName) {
		try (Git git = Git.open(getLocalRepoFile())) {
			git.getRepository().resolve("remotes" + remoteBranchName);
			git.checkout().setCreateBranch(true).setName(newBranchName).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reservation(LocalDateTime dateTime, String localBranchName, String remoteBranchName) {
		try (Git git = Git.open(getLocalRepoFile())) {
			if (isPossibleToPush(localBranchName, remoteBranchName)) {
				List<RevCommit> localCommitList = getLocalCommitList(localBranchName);
				List<RevCommit> noPushCommits = localCommitList.subList(0, localCommitList.size() - getRemotesCommitList(remoteBranchName).size());
				noPushCommits.forEach(c -> System.out.println(" = " + c));

				for (RevCommit revCommit : noPushCommits) {
					PersonIdent authorIdent = revCommit.getAuthorIdent();
					PersonIdent committerIdent = revCommit.getCommitterIdent();
					String fullMessage = revCommit.getFullMessage();
					Date date = Timestamp.valueOf(dateTime);
					revCommit = null;
					revCommit = git.commit().setAmend(true).setMessage(fullMessage)
							.setAuthor(new PersonIdent(authorIdent, date))
							.setAuthor(new PersonIdent(committerIdent, date))
							.call();
				}
			} else {
				System.out.println("no commits not pushed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
