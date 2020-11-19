package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapboxService {
	
	private final static Logger Log = LoggerFactory.getLogger(MapboxService.class);
	private static double lon;
	private static double lat;

	public MapboxService(String address, String postcode) {
		String location = address + " " + postcode;
		
	    if (location.trim().equals("")) {
		   throw new NullPointerException("A valid address is required.");
		}

		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
		  .accessToken("pk.eyJ1IjoiaDA5IiwiYSI6ImNrOHZnYW1iaDBocnkzb213MmJ1cG1jcHMifQ.v_lOAmtB-5cdk4dY_ETVpg")
		  .query(location)
		  .build();
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		
				List<CarmenFeature> results = response.body().features();
		
				if (results.size() > 0) {
				  // Log the first results Point.
				  Point firstResultPoint = results.get(0).center();
				  lon = firstResultPoint.longitude();
				  lat = firstResultPoint.latitude();
				  Log.info("INF", "onResponse: " + firstResultPoint.toString());
		
				} else {
				  // No result for your request were found.
				  Log.info("INF", "onResponse: No result found");
				}
			}
		
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
				throwable.printStackTrace();
			}
		});
	
	}
	
	public double getLon() {
		return lon;
	}
	public double getLat() {
		return lat;
	}

}
