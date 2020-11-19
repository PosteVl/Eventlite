package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;

import javax.validation.Valid;

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

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.MapboxService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}
	  
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String getVenues(Model model) {

		model.addAttribute("venues_arr", venueService.findAll());
		return "venues/new";
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
    public String searchVenues(
            @RequestParam(value = "query", required = false, defaultValue = "") String query, Model model) {

        Iterable<Venue> venues = venueService.findAll();

        ArrayList<Venue> results = new ArrayList<Venue>();

        for(Venue v: venues){
            if(v.getName().contains(query)){
                results.add(v);
            }
        }

        model.addAttribute("searched", query);
        model.addAttribute("venues", results);

        return "venues/search";
    }

	
	@RequestMapping(value= "/new", method = RequestMethod.POST)
	public String createVenue(@RequestBody @Valid @ModelAttribute("venue") Venue venue,
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {

		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/new";
		}
		
		MapboxService mapboxService = new MapboxService(venue.getAddress(), venue.getPostcode());
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		venue.setLongitude(mapboxService.getLon());
		venue.setLatitude(mapboxService.getLat());
			
			venueService.save(venue);
			redirectAttrs.addFlashAttribute("ok_message", "New venue added.");
			model.addAttribute("venues", venueService.findAll());
			return "redirect:/venues";
	}
		
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String info(@PathVariable("id") long id,
			@RequestParam(value = "info", required = false, defaultValue = "Info") String name, Model model) {
				
		Venue venue = venueService.findOne(id);
		model.addAttribute("name", venue.getName());
		model.addAttribute("address", venue.getAddress());
		model.addAttribute("postcode", venue.getPostcode());
		model.addAttribute("capacity", venue.getCapacity());
		model.addAttribute("events", eventService.findAllByVenue(venue));
		
		return "venues/venueInfo";
	}

	
	@RequestMapping(value = "deleteVenue/{id}", method = RequestMethod.DELETE)
	public String deleteVenue(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttrs){
			
			Venue v = venueService.findOne(id);
			
			if(!eventService.findAllByVenue(v).iterator().hasNext()) {
				venueService.deleteById(id);
				redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");
				return "redirect:/venues";
			}
			
			else {
				redirectAttrs.addFlashAttribute("warning_message", "There are at least one event hosting at this venue. Venue can not be deleted.");
				return "redirect:/venues/{id}";
			}
			
	        
	    }
	
	@RequestMapping(value = "/updateVenue/{id}", method = RequestMethod.GET)
	public String getEventToUpdate(Model model, @PathVariable("id") long id) {
		
		Venue venue = venueService.findById(id).get();
		
		model.addAttribute("updateVenue", venue);
//		model.addAttribute("venues", venueService.findAll());
		
		return "venues/updateVenue";
	}
	
	@RequestMapping(value = "/updateVenue/{id}", method = RequestMethod.POST)
	public String updateVenue(@PathVariable("id") long id, Model model, @ModelAttribute Venue v, 
								RedirectAttributes redirectAttrs, BindingResult errors) {
		
//		if (errors.hasErrors()) {
//			model.addAttribute("updateVenue", e);
//		return "venues/updateVenue";
//		}           
		MapboxService mapboxService = new MapboxService(v.getAddress(), v.getPostcode());
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		v.setLongitude(mapboxService.getLon());
		v.setLatitude(mapboxService.getLat());	
		
		venueService.save(v);
		redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");
		model.addAttribute("venues", venueService.findAll());
		
		return "redirect:/venues";
	}
}

