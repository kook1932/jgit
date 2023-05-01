package com.jy.jgit.biz.service;

import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class JgitService {

	/**
	 * 마지막 Contributes 날짜로부터 오늘까지 필요한 커밋 개수 구하기
	 * @param username
	 * @return count(int)
	 */
	public int countForAllContributes(String username, String token) {
		URI uri = UriComponentsBuilder
				.fromUriString("https://api.github.com")
				.path("/graphql")
				.encode()
				.build()
				.toUri();

		RequestEntity<String> requestEntity = RequestEntity
				.post(uri)
				.header("Authorization", "bearer " + token)
				.body(_getAllContributesQuery(username));


		return 0;
	}

	private String _getAllContributesQuery(String username) {
		return "{\n" +
				"        \"query\": query {\n" +
				"            user(login: \"" + username + "\") {\n" +
				"              name\n" +
				"              contributionsCollection {\n" +
				"                contributionCalendar {\n" +
				"                  colors\n" +
				"                  totalContributions\n" +
				"                  weeks {\n" +
				"                    contributionDays {\n" +
				"                      color\n" +
				"                      contributionCount\n" +
				"                      date\n" +
				"                      weekday\n" +
				"                    }\n" +
				"                    firstDay\n" +
				"                  }\n" +
				"                }\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"    }";
	}

}
