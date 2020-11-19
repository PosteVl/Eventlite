package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;
	
	@Test
	public void findAllTest() {
		List<Event> event = (List<Event>) eventService.findAll();
		long totalNumberOfEvents = (long) event.size();
		long count = eventService.count();
		
		assertThat("All events are returned after calling the findAll() method", count, equalTo(totalNumberOfEvents));
	}
	
//	@Test
//	public void findAllByVenueTest() {
//		List<Event> event = (List<Event>) eventService.findAll();
//		Event event1 = new Event();
//		Event event2 = new Event();
//		Venue venue = new Venue();
//		venue.setAddress("Downing Street");
//		event1.setVenue(venue);
//		event2.setVenue(venue);
//		
//		event.add(event1);
//		event.add(event2);
//		
//		assertThat("Testing", eventService.findAllByVenue(venue), event1);
//	}
}
