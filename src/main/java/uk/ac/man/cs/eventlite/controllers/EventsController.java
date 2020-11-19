package uk.ac.man.cs.eventlite.controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.TwitterException;

import java.util.ArrayList;

import javax.validation.Valid;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.TwitterService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;
	
	private TwitterService twitterService = new TwitterService();

	
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model, @RequestParam (value = "search", required = false) String search) throws TwitterException{
		
		try {
			model.addAttribute("tweets", twitterService.getMaxFiveTimelineWithURL());
		}
		catch(TwitterException e) {
			System.err.println(e.getMessage());
			model.addAttribute("tweets", null);
		}
		
		model.addAttribute("events", eventService.findAllByOrderByDateAscTimeAsc());
		
		return "events/index";
	}
	
	
	
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String getVenues(Model model) {
		
		model.addAttribute("venues_arr", venueService.findAll());
		return "events/new";
	}
	
	@RequestMapping(value= "/new", method = RequestMethod.POST)
	public String createEvent(@RequestBody @Valid @ModelAttribute("event") Event event,
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {

//		if (errors.hasErrors()) {
//			model.addAttribute("event", event);
//			return "events/new";
//		}            
			eventService.save(event);
			redirectAttrs.addFlashAttribute("ok_message", "New event added.");
			model.addAttribute("events", eventService.findAll());
			return "redirect:/events";
	}
	
	
	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String info(@PathVariable("id") long id,
			@RequestParam(value = "info", required = false, defaultValue = "Info") String name, Model model) {
				
		Event event = eventService.findOne(id);
		model.addAttribute("name", event.getName());
		model.addAttribute("date", event.getDate());
		model.addAttribute("time", event.getTime());
		model.addAttribute("venue", event.getVenue());
		model.addAttribute("description", event.getDescription());
		
		return "events/eventInfo";
	} 
	
	
	
	@RequestMapping(value = "delete/{id}", method = RequestMethod.DELETE)
    public String deleteMovie(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttrs){
		
		eventService.deleteById(id);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
		
        return "redirect:/events";
    }
	
	
	
	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
	public String postTweet(@RequestParam(value = "tweet", required = false, defaultValue = "")String tweet, @PathVariable("id") long id, Model model, @ModelAttribute Event event, RedirectAttributes redirectAttrs) throws TwitterException{
		
		String thisTweet = twitterService.createTweet(tweet);
		
		if(thisTweet != null) {
			String okMessage = "Tweet: \"" + tweet + "\" successfully posted!";
			redirectAttrs.addFlashAttribute("ok_message", okMessage);
		}
		else {
			redirectAttrs.addFlashAttribute("error_message", "This tweet has already been posted");
		}
		
		
		return "redirect:/events/" + id;
	}
	
	
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String getEventToUpdate(Model model, @PathVariable("id") long id) {
		
		Event event = eventService.findById(id).get();
		
		model.addAttribute("updateThisEvent", event);
		model.addAttribute("venues", venueService.findAll());
		
		return "events/update";
	}
	
	@RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
	public String updateEvent(@PathVariable("id") long id, Model model, @ModelAttribute Event e, 
								RedirectAttributes redirectAttrs, BindingResult errors) {
		
//		if (errors.hasErrors()) {
//			model.addAttribute("updateThisEvent", e);
//		return "events/update";
//		}           
		
		eventService.save(e);
		redirectAttrs.addFlashAttribute("ok_message", "Event updated.");
		model.addAttribute("events", eventService.findAll());
		
		return "redirect:/events";
	}
	
	
	
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String searchEvents(
			@RequestParam(value = "query", required = false, defaultValue = "") String query, Model model) {

		// get all past and upcoming events
		Iterable<Event> upcomingEvents = eventService.findUpcoming();
		Iterable<Event> pastEvents = eventService.findPrevious();
		// to be stored in array lists
		ArrayList<Event> upcomingResults = new ArrayList<Event>();
		ArrayList<Event> pastResults = new ArrayList<Event>();
		
		
        // see if event is past or upcoming
		for(Event e: upcomingEvents){
			if(e.getName().contains(query)){
				upcomingResults.add(e);
			}
		}

		for(Event e: pastEvents){
			if(e.getName().contains(query)){
				pastResults.add(e);
			}
		}
		
		// update the view
		model.addAttribute("searched", query);
		model.addAttribute("upcomingEvents", upcomingResults);
		model.addAttribute("pastEvents", pastResults);

		return "events/search";
	}
	
	
}

