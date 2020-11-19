package uk.ac.man.cs.eventlite.dao;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long>{

	
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();

	public Iterable<Event> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate thisDay);
	
	
	public Iterable<Event> findByDateLessThan(LocalDate thisDay);

	public Iterable<Event> findByNameContainingOrderByDateAscNameAsc(String name);

	public Iterable<Event> findAllByVenueOrderByDateAscNameAsc(Venue venue);

	public Iterable<Event> findTop3ByDateGreaterThanEqualOrderByDateAscNameAsc(LocalDate thisDay);
}
