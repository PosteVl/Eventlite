package uk.ac.man.cs.eventlite.controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.ArrayList;
import java.util.List;


import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(produces = { MediaType.TEXT_HTML_VALUE })
public class IndexController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String redirectHome(Model model){
		return "redirect:/homepage";
	}

	
	@RequestMapping(value = "homepage", method = RequestMethod.GET)
	public String getHomePage(Model model){

		//get next three events
		if (eventService.count() > 0) {
			List<Event> events = (List<Event>) eventService.findUpcoming();
			events = events.subList(0, 3);
			model.addAttribute("events", events);
		}
		
		
		
		//get three venues with most events
		int v1Size, v2Size, v3Size;
		v1Size = 0; v2Size = 0; v3Size = 0;
		Venue v1, v2, v3;
		v1 = new Venue(); v2 = new Venue(); v3 = new Venue();
		int eventsForVenue;
		
		for (Venue v: venueService.findAll()) {
			eventsForVenue = ((List<Event>) eventService.findAllByVenue(v)).size();
			
			if (eventsForVenue >= v1Size) {
				v3Size = v2Size; 
				v3 = v2;
				v2Size = v1Size;
				v2 = v1;
				v1Size = eventsForVenue;
				v1 = v;
			}
			else if (eventsForVenue >= v2Size) {
				v3Size = v2Size;
				v3 = v2;
				v2Size = eventsForVenue;
				v2 = v;
			}
			else if (eventsForVenue >= v3Size) {
				v3 = v;
				v3Size = eventsForVenue;
			}
		}
		
		List<Venue> topThreeVenues = new ArrayList<>();
		topThreeVenues.add(v1);
		topThreeVenues.add(v2);
		topThreeVenues.add(v3);
		
		model.addAttribute("venues", topThreeVenues);
		
		
		return "home/homepage";
	}
	
	
}

