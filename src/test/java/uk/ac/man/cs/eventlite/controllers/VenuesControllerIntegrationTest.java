package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
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
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	private HttpEntity<String> httpEntity;
	private String baseUrl;
	private String loginUrl;
	private String venueUrl;
	private String updateVenueUrl;
	private String deleteVenueUrl;
	private String deleteVenueUrl7;
	private String getVenueUrl1;
	private String getVenueUrl7;
	
	// An anonymous and stateless log in.
	private final TestRestTemplate anon = new TestRestTemplate();

	@Autowired
	private TestRestTemplate template;

	@BeforeEach
	public void setup() {
		this.venueUrl = "http://localhost:" + port + "/venues/new";
		this.getVenueUrl1 = "http://localhost:" + port + "/venues/1";
		this.getVenueUrl7 = "http://localhost:" + port + "/venues/7";
		this.updateVenueUrl = "http://localhost:" + port + "/venues/updateVenue/1?";
		this.deleteVenueUrl = "http://localhost:" + port + "/venues/deleteVenue/1";
		this.deleteVenueUrl7 = "http://localhost:" + port + "/venues/deleteVenue/7";
		this.baseUrl = "http://localhost:" + port + "/venues";
		this.loginUrl = "http://localhost:" + port + "/sign-in";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}

	@Test
	public void testGetAllVenues() {
		ResponseEntity<String> response = template.exchange("/venues", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void getLoginForm() {
		get(loginUrl, "_csrf");
	}
	
	private void get(String url, String expectedBody) {
		ResponseEntity<String> response = anon.exchange(url, HttpMethod.GET, httpEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
		assertThat(response.getBody(), containsString(expectedBody));
	}
	
	@Test
	public void testLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Markel");
		login.add("password", "Vigo");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith(":" + this.port + "/"));
	}
	
	@Test
	public void testBadUserLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Robert");
		login.add("password", "Haines");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith("/sign-in?error"));
	}

	@Test
	public void testBadPasswordLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Caroline");
		login.add("password", "J");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith("/sign-in?error"));
	}
	
	private String getCsrfToken(String body) {
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);

		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
	
	@Test
	public void postVenueNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();		
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);

		ResponseEntity<String> response = anon.exchange(venueUrl, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
	}
	
	@Test 
	public void updateVenueNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		// GET venue 1
		ResponseEntity<String> response_1 = template.exchange(getVenueUrl1, HttpMethod.GET, httpEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));
 
		// UPDATE venue 1
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();		
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);
		ResponseEntity<String> response = anon.exchange(updateVenueUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
	}
	
	@Test
	@DirtiesContext
	public void postVenueWithLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the new venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		// Populate the new venue form.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

		// POST the new venue
		ResponseEntity<String> response = template.exchange(venueUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), containsString(baseUrl));
		assertThat(5, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void postVenueWithNoDataWithLogin() {

		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the new venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		// Populate the new venue form.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", null);
		form.add("capacity", null);
		form.add("address", null);
		form.add("postcode", null);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		// POST the new venue
		ResponseEntity<String> response = template.exchange(venueUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void postVenueWithBadDataWithLogin() {

		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the new venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		// Populate the new venue form.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", " ");
		form.add("capacity", " a3");
		form.add("address", " ");
		form.add("postcode", "abc ");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		// POST the new venue
		ResponseEntity<String> response = template.exchange(venueUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void updateVenueWithLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the new venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());
		
		// GET venue 1
		MultiValueMap<String, String> form_1 = new LinkedMultiValueMap<>();
		form_1.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form_1, postHeaders);
		ResponseEntity<String> response_1 = template.exchange(getVenueUrl1, HttpMethod.GET, postEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));

		// UPDATE venue 1
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "P Sherman");
		form.add("capacity", "42");
		form.add("address", "42 Wallaby Way, Sydney");
		form.add("postcode", "PS4 2WW");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

		// POST the UPDATED venue 1
		ResponseEntity<String> response = template.exchange(updateVenueUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), containsString(baseUrl));
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	// An event is hosted at venue 1, thus cannot be deleted
	@Test
	@DirtiesContext
	public void deleteNotAllowedVenueWithLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());
		
		// GET venue 1
		MultiValueMap<String, String> form_1 = new LinkedMultiValueMap<>();
		form_1.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form_1, postHeaders);
		ResponseEntity<String> response_1 = template.exchange(getVenueUrl1, HttpMethod.GET, postEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));

		// DELETE venue 1 
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
	
		ResponseEntity<String> response = template.exchange(deleteVenueUrl, HttpMethod.DELETE, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		// Venue 1 not actually deleted because an event is hosted at it.
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
	
	// No events are hosted at venue 7, thus can be deleted
	@Test
	@DirtiesContext
	public void deleteAllowedVenueWithLogin() {
		template = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		// Set the session cookie and populate the log in form.
		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// Log in.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = template.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the venue form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());
		
		// GET venue 7
		MultiValueMap<String, String> form_1 = new LinkedMultiValueMap<>();
		form_1.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form_1, postHeaders);
		ResponseEntity<String> response_1 = template.exchange(getVenueUrl7, HttpMethod.GET, postEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));

		// DELETE venue 7
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
	
		ResponseEntity<String> response = template.exchange(deleteVenueUrl7, HttpMethod.DELETE, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		// Venue 7 is deleted
		assertThat(3, equalTo(countRowsInTable("venues")));
	}
	
	@Test 
	public void deleteAllowedVenueNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		// GET venue 7
		ResponseEntity<String> response_1 = template.exchange(getVenueUrl7, HttpMethod.GET, httpEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));
 
		// DELETE venue 7
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);
		
		ResponseEntity<String> response = anon.exchange(deleteVenueUrl7, HttpMethod.DELETE, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
		// Venue 7 is not deleted
		assertThat(4, equalTo(countRowsInTable("venues")));
	}
}