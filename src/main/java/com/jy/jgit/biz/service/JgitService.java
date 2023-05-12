package com.jy.jgit.biz.service;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

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
				.header("content-type", "application/json")
				.header("Authorization", "bearer " + token)
				.body(_getAllContributesQuery(username));

		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> exchange = restTemplate.exchange(requestEntity, String.class);
			System.out.println("exchange = " + exchange);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	private String _getAllContributesQuery(String username) {
		return "{\n" +
				"\"query\": \"query { user(login: \"" + username + "\") { name contributionsCollection { contributionCalendar { colors totalContributions weeks { contributionDays { color contributionCount date weekday } firstDay } } } }}\"\n" +
				"}";
	}

}
