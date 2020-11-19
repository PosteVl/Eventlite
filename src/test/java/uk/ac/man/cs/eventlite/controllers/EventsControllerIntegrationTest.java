package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;

import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
	// We need cookies for Web log in.
	// Initialize this each time we need it to ensure it's clean.
	private TestRestTemplate stateful;

	@Autowired
	@Mock
	private EventService eventService;


	@InjectMocks
	private EventsController eventsController;
	
	// An anonymous and stateless log in.
	private final TestRestTemplate anon = new TestRestTemplate();
	
	@LocalServerPort
	private int port;

	private String baseUrl;
	private String loginUrl;
	private String eventUrl;
	
	private static final String INDEX = "/1";

	@BeforeEach
	public void setup() {
		
		this.baseUrl = "http://localhost:" + port + "/events";
		this.loginUrl = "http://localhost:" + port + "/sign-in";
		this.eventUrl = baseUrl + INDEX;

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		httpEntity = new HttpEntity<String>(headers);
	}

	private void get(String url, String expectedBody) {
		ResponseEntity<String> response = anon.exchange(url, HttpMethod.GET, httpEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
		assertThat(response.getBody(), containsString(expectedBody));
	}
	
	private String getCsrfToken(String body) {
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);

		// matcher.matches() must be called!
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}


	@Test
	public void getLoginForm() {
		get(loginUrl, "_csrf");
	}

	@Test
	public void testGetAllEvents() {
		ResponseEntity<String> response = template.exchange("/events", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}

	@Test
	public void testLogin() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
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
		ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith(":" + this.port + "/"));
	}


	
	@Test
	public void postAddEventgNoLogin() {

		ResponseEntity<String> response = anon.exchange(baseUrl + "/new", HttpMethod.POST, null, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
	}
	
	@Test
	public void postAddEventWithLoginBadData() {
		String csrfToken = (String) testUserLogin()[0];
		HttpHeaders postHeaders = (HttpHeaders) testUserLogin()[1];
		
		// Populate the new event form.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", " ");
     	form.add("date", "2");
		form.add("time", "2");
		form.add("venue", " ");
		form.add("description", " ");
		

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<>(form, postHeaders);

		// POST the new event
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/new", HttpMethod.POST, postEntity, String.class);

		// It should fail
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}
	
	public void postAddEventWithLoginNoData() {
		String csrfToken = (String) testUserLogin()[0];
		HttpHeaders postHeaders = (HttpHeaders) testUserLogin()[1];
		
		// Populate the new event form.
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", null);
    	form.add("date", null);
		form.add("time", null);
		form.add("venue", null);
		form.add("description", null);
		

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<>(form, postHeaders);

		// POST the new event
		ResponseEntity<String> response = stateful.exchange(baseUrl + "/new", HttpMethod.POST, postEntity, String.class);

		// It should fail
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
	}

	@Test
	public void postAddEventWithLoginGoodData() {
		String csrfToken = (String) testUserLogin()[0];
		HttpHeaders postHeaders = (HttpHeaders) testUserLogin()[1];
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(form, postHeaders);

		ResponseEntity<String> response = stateful.exchange(baseUrl + "/new", HttpMethod.GET, httpEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}
	
	@Test
	public void postUpdateEventgNoLogin() {

		ResponseEntity<String> response = anon.exchange(baseUrl+ "/update/1", HttpMethod.POST, null, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
	}

	@Test
	public void postUpdateEventWithLogin() {
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
		
		Event e = new Event();
		int id = countRowsInTable("event") + 1;
		e.setId(id);
		e.setName("temp");
		eventService.save(e);
		
		// GET venue 1
		MultiValueMap<String, String> form_1 = new LinkedMultiValueMap<>();
		form_1.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form_1, postHeaders);
		ResponseEntity<String> response_1 = template.exchange(baseUrl + "/" + id, HttpMethod.GET, postEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));

		// UPDATE venue 1
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "P Sherman");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

		// POST the UPDATED venue 1
		ResponseEntity<String> response = template.exchange(baseUrl + "/update/" + id + "?", HttpMethod.POST, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), containsString(baseUrl));
		//assertThat(4, equalTo(countRowsInTable("venues")));

	} 
	
	@Test
	public void postDeleteEventgNoLogin() {

		ResponseEntity<String> response = anon.exchange(baseUrl+ "/delete/1", HttpMethod.DELETE, null, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
	}
	

	@Test
	public void postDeleteEventWithLogin() {
		
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

		// Set the session cookie and GET the event form so we can read the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = template.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());
		
		Event e = new Event();
		int id = countRowsInTable("event") + 1;
		e.setId(id);
		e.setName("temp");
		eventService.save(e);
		
		// GET event 1
		MultiValueMap<String, String> form_1 = new LinkedMultiValueMap<>();
		form_1.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form_1, postHeaders);
		ResponseEntity<String> response_1 = template.exchange(baseUrl + "/" + id, HttpMethod.GET, postEntity, String.class);
		assertThat(response_1.getStatusCode(), equalTo(HttpStatus.OK));

		// DELETE event 1
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
	
		ResponseEntity<String> response = template.exchange(baseUrl + "/delete/" + id, HttpMethod.DELETE, postEntity, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
	} 

	@Test
	public void postTwitterNoLogIn() {
		ResponseEntity<String> response = anon.exchange(baseUrl + "/1", HttpMethod.POST, null, String.class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(Objects.requireNonNull(response.getHeaders().getLocation()).toString(), equalTo(loginUrl));
	}	
	
	private Object[] testUserLogin() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		// Set up headers for GETting and POSTing.
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// GET the log in page so we can read the CSRF token and the session
		// cookie.
		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
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
		ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		// Set the session cookie and GET the new greeting form so we can read
		// the new CSRF token.
		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		return new Object[] {getCsrfToken(formResponse.getBody()), postHeaders};

	}


	
	
}
