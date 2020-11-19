package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (eventService.count() > 0) {
			log.info("Database already populated. Skipping data initialization.");
			return;
		}


		// Build and save initial models here.
		Venue venue_1 = new Venue();
		venue_1.setName("Alan Turing Building");
		venue_1.setCapacity(80);
		venue_1.setAddress("Alan Turing Building - School of Mathematics, Oxford Rd");
		venue_1.setPostcode("M13 9TD");
		venue_1.setLatitude(53.468104);
		venue_1.setLongitude(-2.231266);
		venueService.save(venue_1);
		log.info("Added venue (" + venue_1.getId() + "): " + venue_1.getName());
		
		Venue venue_2 = new Venue();
		venue_2.setName("Kilburn, LF31");
		venue_2.setCapacity(60);
		venue_2.setAddress("Kilburn Building, Oxford Road");
		venue_2.setPostcode("M13 9QQ");
		venue_2.setLatitude(53.467524);
        venue_2.setLongitude(-2.233915);
		venueService.save(venue_2);
		log.info("Added venue (" + venue_2.getId() + "): " + venue_2.getName());
		
		Venue venue_3 = new Venue();
		venue_3.setName("University Place");
		venue_3.setCapacity(60);
		venue_3.setAddress("University Place, Oxford Rd");
		venue_3.setPostcode("M13 9GP");
		venue_3.setLatitude(53.466777);
		venue_3.setLongitude(-2.233737);
		venueService.save(venue_3);
		log.info("Added venue (" + venue_3.getId() + "): " + venue_2.getName());
		

		Event new_event = new Event();
		LocalDate date = LocalDate.of(2020,05,11);
		LocalTime time = LocalTime.of(15, 00);
		
		new_event.setName("COMP23412 Showcase, group G");
		new_event.setDate(date);
		new_event.setTime(time);
		new_event.setVenue(venue_1);
		new_event.setDescription("description 1");
		eventService.save(new_event);
		log.info("Added event (" + new_event.getId() + "): ");
	
		Event new_event2 = new Event();
		LocalDate date_2 = LocalDate.of(2020,05,05);
		LocalTime time_2 = LocalTime.of(10, 00);
		
		new_event2.setName("COMP23412 Showcase, group H");
		new_event2.setDate(date_2);
		new_event2.setTime(time_2);
		new_event2.setVenue(venue_2);
		new_event2.setDescription("description 2");
		eventService.save(new_event2);
		log.info("Added event (" + new_event2.getId() + "): ");

		
		Event new_event3 = new Event();
		LocalDate date_3 = LocalDate.of(2020,05,07);
		LocalTime time_3 = LocalTime.of(11, 00);
		
		new_event3.setName("COMP23412 Showcase, group F");
		new_event3.setDate(date_3);
		new_event3.setTime(time_3);
		new_event3.setVenue(venue_3);
		new_event3.setDescription("description 3");
		eventService.save(new_event3);
		log.info("Added event (" + new_event3.getId() + "): ");
		
		// Venue not attached to event - created for integration tests
		Venue venue_4 = new Venue();
		venue_4.setName("Kilburn, Tootil 1");
		venue_4.setCapacity(40);
		venue_4.setAddress("Kilburn Building, Oxford Road");
		venue_4.setPostcode("M13 9QQ");
		venue_4.setLatitude(53.467524);
		venue_4.setLongitude(-2.233915);
		venueService.save(venue_4);
		log.info("Added venue (" + venue_4.getId() + "): " + venue_4.getName());

	}
}
