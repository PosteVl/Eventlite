package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@Mock
	private EventService eventService;

	@Mock
	private EventService venueService;

	@InjectMocks
	private EventsController eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAllByOrderByDateAscTimeAsc()).thenReturn(Collections.<Event> emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAllByOrderByDateAscTimeAsc();
		verifyZeroInteractions(event);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(eventService.findAllByOrderByDateAscTimeAsc()).thenReturn(Collections.<Event> singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAllByOrderByDateAscTimeAsc();
		verifyZeroInteractions(event);
	}
	
	@Test
	public void infoEvent() throws Exception {

		when(eventService.findOne(1)).thenReturn(event);
		
		mvc.perform(MockMvcRequestBuilders.get("/events/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.with(csrf()))
				.andExpect(view().name("events/eventInfo"));
	}
	
	@Test
	public void searchEvent() throws Exception {
	    
	    Iterable<Event> events = eventService.findAll();
	    
	    when(eventService.findAll()).thenReturn(events);
	    
	    mvc.perform(get("/events/search").with(user("Rob").roles(Security.ADMIN_ROLE))
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML)
	        .param("search", "Showcase"))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("searchEvents"))
	        .andExpect(view().name("events/search"));
	    
	    verify(eventService).findAll();
	}
	  
	@Test
	public void searchEventEmptyList() throws Exception {
	    
	    when(eventService.findAll()).thenReturn(Collections.emptyList());
	    
	    mvc.perform(get("/events/search").with(user("Rob").roles(Security.ADMIN_ROLE))
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML)
	        .param("search", "Showcase"))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("searchEvents"))
	        .andExpect(view().name("events/search"));
	}
	
	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}
	
	@Test 
	public void postDeleteEvent() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete("/events/delete/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.with(csrf()))
					.andExpect(status().isFound())
					.andExpect(view().name("redirect:/events")).andExpect(model().hasNoErrors());
		
	}
	
	@Test
	public void postUpdateEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		
		mvc.perform(MockMvcRequestBuilders.post("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("name", "template")
					.param("time", "04:16")
					.param("date", "2020-12-31")
					.param("venue.name", "2")
					.param("description", "This description")
					.accept(MediaType.TEXT_HTML).with(csrf()))
					.andExpect(status().isFound())
					.andExpect(content().string(""))
					.andExpect(view().name("redirect:/events"))
					.andExpect(model().hasNoErrors())
					.andExpect(handler().methodName("updateEvent"))
					.andExpect(flash().attributeExists("ok_message"));
		
		verify(eventService).save(arg.capture());
		assertThat("template", equalTo(arg.getValue().getName()));
		assertThat("04:16", equalTo(arg.getValue().getTime().toString()));
		assertThat("2020-12-31", equalTo(arg.getValue().getDate().toString()));
		assertThat("2", equalTo(arg.getValue().getVenue().getName()));
		assertThat("This description", equalTo(arg.getValue().getDescription()));
		
	}
	
	@Test
	public void postNewEvent() throws Exception {
		ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);

		
		mvc.perform(MockMvcRequestBuilders.post("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("name", "template")
					.param("time", "04:16")
					.param("date", "2020-12-31")
					.param("venue.name", "2")
					.param("description", "This description")
					.accept(MediaType.TEXT_HTML).with(csrf()))
					.andExpect(status().isFound())
					.andExpect(content().string(""))
					.andExpect(view().name("redirect:/events"))
					.andExpect(model().hasNoErrors())
					.andExpect(handler().methodName("createEvent"))
					.andExpect(flash().attributeExists("ok_message"));;
		
		verify(eventService).save(arg.capture());
		assertThat("template", equalTo(arg.getValue().getName()));
		assertThat("04:16", equalTo(arg.getValue().getTime().toString()));
		assertThat("2020-12-31", equalTo(arg.getValue().getDate().toString()));
		assertThat("2", equalTo(arg.getValue().getVenue().getName()));
		assertThat("This description", equalTo(arg.getValue().getDescription()));
		
	}


}
