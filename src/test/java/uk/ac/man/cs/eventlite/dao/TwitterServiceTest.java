package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.TwitterException;

class TwitterServiceTest {
	
	@Autowired
	private TwitterService twitter = new TwitterService();
	
	
	int randomTweetGen = (int) (Math.random()*100000);
	String tweet = "" + randomTweetGen;
	
	@Test
	public void updateStatusTest() throws TwitterException{
		String text = twitter.createTweet(tweet);
		assertEquals(tweet, text);
	}
	
	
	@Test
	public void timelineDisplaysMaxFiveTweetsTest() throws TwitterException{
		int noDisplayed = twitter.getMaxFiveTimelineWithURL().size();
		assertTrue(noDisplayed <= 5);
		assertTrue(noDisplayed >= 0);
	}

}
