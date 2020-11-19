package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;
	
	@LocalServerPort
	private int port;

	private String baseUrl;
	
	// An anonymous log in and a couple of users for basic auth.
	private final TestRestTemplate anon = new TestRestTemplate();
	private final TestRestTemplate evil = new TestRestTemplate("Bad", "Person");
	private final TestRestTemplate user = new TestRestTemplate("Rob", "Haines");

	@Autowired
	private TestRestTemplate template;

	@BeforeEach
	public void setup() {
		this.baseUrl = "http://localhost:" + port + "/api/venues";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		httpEntity = new HttpEntity<String>(headers);
	}

	@Test
	public void testGetAllVenues() {
		ResponseEntity<String> response = template.exchange("/api/venues", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testGetHomepage() {
		ResponseEntity<String> response = template.exchange("/api", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void testGetNewVenue() {
		ResponseEntity<String> response = template.exchange("/api/venues/new", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_ACCEPTABLE));
	}
	
	@Test
	public void postVenueNoAuth() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		postHeaders.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

		ResponseEntity<String> response = anon.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void postVenueBadAuth() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		postHeaders.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

		ResponseEntity<String> response = evil.exchange(baseUrl, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void postVenue() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		postHeaders.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		ResponseEntity<String> response = user.exchange(baseUrl, HttpMethod.GET, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
}