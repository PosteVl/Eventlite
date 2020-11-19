package uk.ac.man.cs.eventlite.dao;

import java.util.*;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public void save(Event event);

	public Iterable<Event> findAll();
	public Iterable<Event> findAllByVenue(Venue venue);

	public Iterable<Event> findAllByOrderByDateAscTimeAsc();

	public Optional<Event> findById(long id);

	public Event findOne(long id);
	public void deleteById(long id);


	public Iterable<Event> findUpcoming();
	
	public Iterable<Event> findPrevious();

	public Iterable<Event> eventListByNameAlphabetically(String name);
	
	public Iterable<Event> findNext3Events(Venue venue);
	
}
