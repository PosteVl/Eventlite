package uk.ac.man.cs.eventlite.dao;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterService {
	
	private Twitter twitter;
	
	public TwitterService() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("syi78XJZHw8UFuphXUTXztJGa")
		  .setOAuthConsumerSecret("8J5acPTK20MbisZbICWzSBenBqZXzHZFsRMZKvvK0j0EsM9fpF")
		  .setOAuthAccessToken("1250790093058703361-2T7Zzfxg8AzmniDA41eLroOm3RGRSo")
		  .setOAuthAccessTokenSecret("zziD6jqZxb79mXf4t0n4S2yKyIhI0hYzlEvhxVdCK7Jrn");
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	
	//method to post a tweet
	public String createTweet(String tweet) throws TwitterException {		
		try {
			Status status = twitter.updateStatus(tweet);
			System.out.println(status.getText());
			return status.getText();
		}
		catch(TwitterException e){
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	
	
	//method to retrieve the timeline
	public List<Status> getTimeline() throws TwitterException {
	     return twitter.getHomeTimeline();
	}
	
	
	
	//method to retrieve 5 of timeline
	public List<Pair<Status, String>> getMaxFiveTimelineWithURL() throws TwitterException{
		try {
			List<Status> allTweets = getTimeline();
			Iterator<Status> statItr = allTweets.iterator();
			List<Pair<Status, String>> tweetMap = new ArrayList<>();
			int count = 0;
			while(statItr.hasNext() && count < 5) {
				Status thisStatus = statItr.next();
				//add status and url to map
				tweetMap.add(getStatusURL(thisStatus));
				count++;
			}
			return tweetMap;
		}
		catch(NullPointerException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}


	
	//method to get URL of a tweet 
	public Pair<Status, String> getStatusURL(Status status) throws TwitterException{
		long ID = status.getId();
		String URL = "https://twitter.com/Eventliteh09_20/status/" + Long.toString(ID);
		Pair<Status, String> statuswithURL = new MutablePair<>(status, URL);
		return statuswithURL;
		
	}

}
