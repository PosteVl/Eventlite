package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;

import retrofit2.Response;

public class MapboxServiceTest {
	  
	  @Test
	  public void queryAcceptsAddress() throws Exception {
	    MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
	      .accessToken("pk.eyJ1IjoiaDA5IiwiYSI6ImNrOHZnYW1iaDBocnkzb213MmJ1cG1jcHMifQ.v_lOAmtB-5cdk4dY_ETVpg")
	      .query("Kilburn Building, Oxford Road M13 9QQ")
	      .build();
	    assertNotNull(mapboxGeocoding);
	    Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
	    List<CarmenFeature> results = response.body().features();
	    Point firstResultPoint = results.get(0).center();
	    assertEquals(-2.233915, firstResultPoint.longitude());
	    assertEquals(53.467524, firstResultPoint.latitude());
	    assertEquals(200, response.code());
	  }

	  @Test 
	  public void invalidAddress() throws ServicesException {

		  NullPointerException thrown =  assertThrows(NullPointerException.class, ()-> { 
	    	  new MapboxService("","");
	    	  try { Thread.sleep(1500); } catch (InterruptedException e) { e.printStackTrace(); }	  
	      });
		  assertTrue(thrown.getMessage().equals("A valid address is required.")); 		  
	  }
	  
	  @Test
	  public void nonExistingAddress() throws Exception {
	    MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
	      .accessToken("pk.eyJ1IjoiaDA5IiwiYSI6ImNrOHZnYW1iaDBocnkzb213MmJ1cG1jcHMifQ.v_lOAmtB-5cdk4dY_ETVpg")
	      .query("M12345")
	      .build();
	    assertNotNull(mapboxGeocoding);
	    Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
	    List<CarmenFeature> results = response.body().features();
	    assertEquals(0, results.size());
	  }
	  
}
