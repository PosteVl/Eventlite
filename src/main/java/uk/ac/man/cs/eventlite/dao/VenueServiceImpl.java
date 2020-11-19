package uk.ac.man.cs.eventlite.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
	private VenueRepository venueRepository;
	
	
	@Override
	public long count() {		
		return venueRepository.count();
	} //count

	@Override
	public Iterable<Venue> findAll() {	
		return venueRepository.findAllByOrderByNameAsc();
	} //findAll
	
	@Override
	public void save(Venue venue) {
		venueRepository.save(venue);
	} //save

	@Override
	public Optional<Venue> findById(long id) {
		return venueRepository.findById(id);
	}

	@Override
	public Venue findOne(long id) {
		return findById(id).orElse(null);
	}
	
	public void deleteById(long id) {
		venueRepository.deleteById(id);
	}
	
}
