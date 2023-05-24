package com.jy.jgit.biz.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.errors.IllegalTodoFileModification;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.eclipse.jgit.api.RebaseCommand.*;

@Slf4j
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
	 * Git Push Method<br>
	 * 	- 현재 커밋들을 remote/branchName 에 push 한다<br>
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

	/**
	 * Remote 에 Push 된 Commits 조회
	 * @param remoteBranchName(ex. orgin/master)
	 * @return List<RevCommit>
	 */
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

	public int getNoPushCommitsCount(String localBranchName, String remoteBranchName) {
		return getLocalCommitList(localBranchName).size() - getRemotesCommitList(remoteBranchName).size();
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
				noPushCommits.forEach(c -> log.info("no Push Commit : {}", c.getFullMessage()));

				for (int i = noPushCommits.size() - 1; i >=  0; i--) {
					RevCommit revCommit = noPushCommits.get(i);
					LocalDateTime afterDateTime = dateTime.plusDays(noPushCommits.size() - i + 1);
					System.out.println("afterDateTime = " + afterDateTime);

					PersonIdent authorIdent = revCommit.getAuthorIdent();
					PersonIdent committerIdent = revCommit.getCommitterIdent();
					String fullMessage = revCommit.getFullMessage();
					Date date = Timestamp.valueOf(afterDateTime);

					revCommit = null;
					revCommit = git.commit().setAmend(true).setMessage(fullMessage)
							.setAuthor(new PersonIdent(authorIdent, date))
							.setAuthor(new PersonIdent(committerIdent, date))
							.call();
				}
			} else {
				log.error("no commits not pushed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int rebaseCommitDate(LocalDateTime dateTime, String localBranchName, String remoteBranchName) {
		if (isPossibleToPush(localBranchName, remoteBranchName)) {
			// local commit logs
			List<RevCommit> localCommitList = getLocalCommitList(localBranchName);

			// origin/master 에 push 되지 않은 commits
			List<RevCommit> noPushCommits = localCommitList.subList(0, localCommitList.size() - getRemotesCommitList(remoteBranchName).size());
			Collections.reverse(noPushCommits);
			noPushCommits.forEach(c -> log.info("no Push Commit : {}", c.getFullMessage()));

			try (Git git = Git.open(getLocalRepoFile())) {
				InteractiveHandler handler = new InteractiveHandler() {
					@Override
					public void prepareSteps(List<RebaseTodoLine> steps) {
						// the handler receives the list of commits that are rebased, i.e. the ones on the local branch
						for(RebaseTodoLine step : steps) {
							// for each step, you can decide which action should be taken
							// default is PICK
							try {
								// by selecting "EDIT", the rebase will stop and ask you to edit the commit-contents
								step.setAction(RebaseTodoLine.Action.EDIT);
							} catch (IllegalTodoFileModification e) {
								throw new IllegalStateException(e);
							}
						}
					}

					@Override
					public String modifyCommitMessage(String oldMessage) {
						return oldMessage;
					}
				};

				// git rebase 수행
				System.out.println("noPushCommits.get(0).getName() = " + noPushCommits.get(0).getName());
				RebaseResult result = git.rebase().setUpstream(noPushCommits.get(0).getName()).runInteractively(handler).call();

				for (int i = 0; i < noPushCommits.size(); i++) {
					RevCommit revCommit = noPushCommits.get(i);
					LocalDateTime afterDateTime = dateTime.plusDays(i);
					System.out.println("afterDateTime = " + afterDateTime);

					PersonIdent authorIdent = revCommit.getAuthorIdent();
					PersonIdent committerIdent = revCommit.getCommitterIdent();
					String fullMessage = revCommit.getFullMessage();

					Date date = Timestamp.valueOf(afterDateTime);
					PersonIdent newAuthor = new PersonIdent(authorIdent, date);
					PersonIdent bewCommitterIdent = new PersonIdent(committerIdent, date);

					revCommit = null;
					revCommit = git.commit().setAmend(true).setMessage(fullMessage)
							.setAuthor(newAuthor)
							.setAuthor(bewCommitterIdent)
							.call();

				}


			} catch (Exception e) {
				log.error("error : {}", e.getMessage());
			}

			return noPushCommits.size();
		} else return 0;
	}

	/**
	 * 리모트 브랜치에 푸시된 마지막 커밋 날짜로부터 금일까지 필요한 커밋의 개수 조회
	 * @return 필요한 커밋 개수(int)
	 */
	public int getCountNotCommit() {
		// Remote Commit List 조회
		List<RevCommit> remotesCommitList = getRemotesCommitList("origin/master");
		int days = 0;

		// 가장 최근 푸시된 커밋의 날짜 조회
		if (!remotesCommitList.isEmpty()) {
			PersonIdent authorIdent = remotesCommitList.get(0).getAuthorIdent();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			log.info("last commit time : {}", dateFormat.format(authorIdent.getWhen()));

			LocalDate nowLocalDate = LocalDate.now();
			LocalDate lastCommitDate = LocalDate.ofInstant(authorIdent.getWhen().toInstant(), ZoneId.systemDefault());

			// 오늘 날짜와 비교하여 일수 계산하여 return
			days = Period.between(lastCommitDate, nowLocalDate).getDays();
		}

		return days;
	}

}
