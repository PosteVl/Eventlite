package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;






@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerTest {

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
	private VenueService venueService;

	@InjectMocks
	private VenuesController venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues/new").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/new")).andExpect(handler().methodName("getVenues"));

		verify(venueService).findAll();
		verifyZeroInteractions(venue); 
	}
	
	@Test
	public void searchVenue() throws Exception {
	    
	    Iterable<Venue> venues = venueService.findAll();
	    
	    when(venueService.findAll()).thenReturn(venues);
	    
	    mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE))
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML)
	        .param("search", "Kilburn"))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("searchVenues"))
	        .andExpect(view().name("venues/search"));
	    
	    verify(venueService, times(2)).findAll();
	}
	  
	@Test
	public void searchVenueEmptyList() throws Exception {
	    
	    when(venueService.findAll()).thenReturn(Collections.emptyList());
	    
	    mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE))
	        .contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML)
	        .param("search", "Kilburn"))
	        .andExpect(status().isOk())
	        .andExpect(handler().methodName("searchVenues"))
	        .andExpect(view().name("venues/search"));
	    
	    verify(venueService).findAll();
	}

	@Test
	public void postVenueNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/venues").contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
		.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(venueService, never()).save(venue);
	}
	
	@Test
	public void testDeleteVenueNoEvents() throws Exception {
		
		when(eventService.findAllByVenue(venue)).thenReturn(Collections.<Event> emptyList());
		
		when(venueService.findOne(1)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.delete("/venues/deleteVenue/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.with(csrf()))
					.andExpect(status().is(302))
					.andExpect(view().name("redirect:/venues"))
					.andExpect(model().hasNoErrors())
					.andExpect(flash().attributeExists("ok_message"));
		
		verify(venueService).deleteById(1);
	}
	
	@Test
	public void testDeleteVenueHasEvents() throws Exception {
		
		when(eventService.findAllByVenue(venue)).thenReturn(Collections.singletonList(event));
		
		when(venueService.findOne(1)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.delete("/venues/deleteVenue/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.with(csrf()))
					.andExpect(view().name("redirect:/venues/{id}"))
					.andExpect(flash().attributeExists("warning_message"));

		verify(venueService, never()).deleteById(1);
	}
	
	@Test
	public void infoVenue() throws Exception {

		when(venueService.findOne(1)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.get("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.with(csrf()))
					.andExpect(view().name("venues/venueInfo"));
	}
	 
	@Test
	public void postUpdateVenue() throws Exception {
		ArgumentCaptor<Venue> args = ArgumentCaptor.forClass(Venue.class);

		
		mvc.perform(MockMvcRequestBuilders.post("/venues/updateVenue/1").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("name", "P Sherman")
					.param("capacity", "42")
					.param("address", "42 Wallaby Way, Sydney")
					.param("postcode", "PS4 2WW")
					.accept(MediaType.TEXT_HTML).with(csrf()))
					.andExpect(status().isFound())
					.andExpect(content().string(""))
					.andExpect(view().name("redirect:/venues"))
					.andExpect(model().hasNoErrors())
					.andExpect(handler().methodName("updateVenue"))
					.andExpect(flash().attributeExists("ok_message"));;
		
		
					
		verify(venueService).save(args.capture());
		
		
		String idString = Long.toString(args.getValue().getId());
		String capacityString = Integer.toString(args.getValue().getCapacity());
		
		assertThat("1", equalTo(idString));
		assertThat("P Sherman", equalTo(args.getValue().getName()));
		assertThat("42", equalTo(capacityString));
		assertThat("42 Wallaby Way, Sydney", equalTo(args.getValue().getAddress()));
		assertThat("PS4 2WW", equalTo(args.getValue().getPostcode()));
	}
	
	@Test 
	public void postNewVenue() throws Exception {
		ArgumentCaptor<Venue> args = ArgumentCaptor.forClass(Venue.class);

		
		mvc.perform(MockMvcRequestBuilders.post("/venues/new").with(user("Rob").roles(Security.ADMIN_ROLE))
					.accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("name", "P Sherman")
					.param("capacity", "42")
					.param("address", "42 Wallaby Way, Sydney")
					.param("postcode", "PS4 2WW")
					.accept(MediaType.TEXT_HTML).with(csrf()))
					.andExpect(status().isFound())
					.andExpect(content().string(""))
					.andExpect(view().name("redirect:/venues"))
					.andExpect(model().hasNoErrors())
					.andExpect(handler().methodName("createVenue"))
					.andExpect(flash().attributeExists("ok_message"));;
		
		
					
		verify(venueService).save(args.capture());
		
		
		String idString = Long.toString(args.getValue().getId());
		String capacityString = Integer.toString(args.getValue().getCapacity());
		
		assertThat("0", equalTo(idString));
		assertThat("P Sherman", equalTo(args.getValue().getName()));
		assertThat("42", equalTo(capacityString));
		assertThat("42 Wallaby Way, Sydney", equalTo(args.getValue().getAddress()));
		assertThat("PS4 2WW", equalTo(args.getValue().getPostcode()));
	}

}
