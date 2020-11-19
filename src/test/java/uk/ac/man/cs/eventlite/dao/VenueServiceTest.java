package uk.ac.man.cs.eventlite.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;

import java.util.List;

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

public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;
	
	private VenueRepository mockVenueRepository;
	
	@Test
	public void findAllTest() {
		List<Venue> venue = (List<Venue>) venueService.findAll();
		long totalNumberOfVenues = (long) venue.size();
		long count = venueService.count();
		
		assertThat("All venues are returned after calling the findAll() method", count, equalTo(totalNumberOfVenues));
	}
	
//	@Test
//	public void findByIdTest() {
//		long id = 28;
//		List<Venue> venue = (List<Venue>) venueService.findAll();
//		Venue venue1 = new Venue();
//		venue1.setId(id);
//		venue.add(venue1);
//		
//		when(venueService.findOne(id)).thenReturn(venue1);
//		
//		Venue returnedVenue = venueService.findOne(id);
//		
//		verify(venueService, times(1)).findOne(id);
//		verifyNoMoreInteractions(venueService);
//		
//		assertEquals(venue1, returnedVenue);
//	}


}
