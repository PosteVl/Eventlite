package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	// use this to get previous and upcoming events
	private LocalDate thisDay = LocalDate.now();

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public void save(Event event) {
		eventRepository.save(event);
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findAll();
	}

	@Override
	public Iterable<Event> findAllByOrderByDateAscTimeAsc(){
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	@Override
	public Iterable<Event> findAllByVenue(Venue venue) {
		return eventRepository.findAllByVenueOrderByDateAscNameAsc(venue);
	}

	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}

	@Override
	public Event findOne(long id) {
		return findById(id).orElse(null);
	}

	public void deleteById(long id) {
		eventRepository.deleteById(id);
	}

	@Override
	public Iterable<Event> findUpcoming(){
		return eventRepository.findByDateGreaterThanEqualOrderByDateAscTimeAsc(thisDay);
	}

	@Override
	public Iterable<Event> findPrevious(){
		return eventRepository.findByDateLessThan(thisDay);
	}

	@Override
	public Iterable<Event> eventListByNameAlphabetically(String name){
		return eventRepository.findByNameContainingOrderByDateAscNameAsc(name);
	}
	
	@Override
	public Iterable<Event> findNext3Events(Venue venue){
		return eventRepository.findTop3ByDateGreaterThanEqualOrderByDateAscNameAsc(thisDay);
	}

}
