package com.jy.jgit.biz.domain;

import lombok.Builder;
import lombok.Data;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class GitDomain {

	private String username;
	private String userToken;
	private String dirPath;

	private String repoName;
	private String branchName;

	private CredentialsProvider credentialsProvider;

	@Builder
	public GitDomain(String username, String userToken, String dirPath, String repoName, String branchName) {
		this.username = username;
		this.userToken = userToken;
		this.dirPath = dirPath;
		this.repoName = repoName;
		this.branchName = branchName;
		this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, userToken);    // set CredentialsProvider
	}

	// dirPath 에 존재하는 File 객체 Return
	public File getLocalRepoFile() {
		return new File(dirPath);
	}

	// git push method
	public void push() {
		try (Git git = Git.open(getLocalRepoFile())) {
			git.push()
					.setCredentialsProvider(credentialsProvider)
					.setRemote(repoName)
					.setRefSpecs(new RefSpec(branchName))
					.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// List all commits in a repository
	public List<RevCommit> getLocalCommitList() {
		List<RevCommit> list = new ArrayList<>();
		try (Git git = Git.open(getLocalRepoFile())) {
			Iterable<RevCommit> revCommits = git.log()
					.add(git.getRepository().resolve("master"))
					.call();
			revCommits.iterator().forEachRemaining(list::add);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<RevCommit> getRemotesCommitList() {
		List<RevCommit> list = new ArrayList<>();
		try (Git git = Git.open(getLocalRepoFile())) {
			Iterable<RevCommit> revCommits = git.log()
					.add(git.getRepository().resolve("remotes/origin/master"))
					.call();
			revCommits.iterator().forEachRemaining(list::add);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean isPossibleToPush() {
		return getLocalCommitList().size() - getRemotesCommitList().size() > 0;
	}

	public void checkoutRemoteBranchInNewBranch() {
		try (Git git = Git.open(getLocalRepoFile())) {
			git.getRepository().resolve("remotes/origin/master");
			git.checkout().setCreateBranch(true).setName("cherry-branch").call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reservation() {
		try (Git git = Git.open(getLocalRepoFile())) {
			if (isPossibleToPush()) {
				List<RevCommit> localCommitList = getLocalCommitList();
				List<RevCommit> noPushCommits = getLocalCommitList().subList(0, localCommitList.size() - getRemotesCommitList().size());
				noPushCommits.forEach(c -> System.out.println(" = " + c));

				for (RevCommit revCommit : noPushCommits) {
					PersonIdent authorIdent = revCommit.getAuthorIdent();
					PersonIdent committerIdent = revCommit.getCommitterIdent();
					String fullMessage = revCommit.getFullMessage();
					Date date = new Date();
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
