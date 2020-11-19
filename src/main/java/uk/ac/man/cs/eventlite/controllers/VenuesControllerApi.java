package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@RequestMapping(method = RequestMethod.GET)
	public Resources<Resource<Venue>> getAllVenues() {
		return venueToResource(venueService.findAll());
	}
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public ResponseEntity<?> newVenue() {
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
	}
	
	// add an event
	@RequestMapping(value = "/new", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createVenue(@RequestBody @Valid Venue venue, BindingResult result) {

		if (result.hasErrors()) {
			return ResponseEntity.unprocessableEntity().build();
		}

		venueService.save(venue);
		URI location = linkTo(VenuesControllerApi.class).slash(venue.getId()).toUri();

		return ResponseEntity.created(location).build();
	}

	private Resource<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();

		return new Resource<Venue>(venue, selfLink);
	}
	
	private Resource<Event> eventToResource(Event event) {
		Link selfLink = linkTo(EventsControllerApi.class).slash(event.getId()).withSelfRel();

		return new Resource<Event>(event, selfLink);
	}
	
	private Resources<Resource<Event>> eventToResource(Iterable<Event> events, long id) {

        List<Resource<Event>> resources = new ArrayList<Resource<Event>>();
        for (Event event : events) {
            resources.add(eventToResource(event));
        }

        return new Resources<Resource<Event>>(resources,
                linkTo(VenuesControllerApi.class).slash(id).slash("events").withSelfRel(),
                linkTo(VenuesControllerApi.class).slash(id).withRel("venue"),
                linkTo(VenuesControllerApi.class).slash(id).slash("events").withRel("events"));
    }

	private Resources<Resource<Venue>> venueToResource(Iterable<Venue> venues) {
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();

		List<Resource<Venue>> resources = new ArrayList<Resource<Venue>>();
		for (Venue venue : venues) {
			resources.add(venueToResource(venue));
		}

		return new Resources<Resource<Venue>>(resources, selfLink);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Venue> delete(@PathVariable("id") long id){
		/*Venue venue = venueService.findOne(id);
		
		if(eventService.findAllByVenue(venue) != Collections.<Event> emptyList())
			return ResponseEntity.ok().build();
		*/
		return ResponseEntity.noContent().build();
	}
	
	@RequestMapping(value = "/{id}/events/next3events", method = RequestMethod.GET)
    public Resources<Resource<Event>> getOnesUpcomingEvents(@PathVariable("id") long id) {
		return eventToResource(eventService.findNext3Events(venueService.findOne(id)), id);
    }

}
