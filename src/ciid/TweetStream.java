package ciid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import toxi.util.datatypes.DoubleRange;

@XmlRootElement(name = "tweets")
public class TweetStream {

	@XmlElement(name = "tweet")
	List<Tweet> tweets = new ArrayList<Tweet>();

	/**
	 * Groups all tweets by username and returns a map using usernames as lookup
	 * keys.
	 * 
	 * @return map of user names with list of user tweets as their value
	 */
	public HashMap<String, List<Tweet>> extractUsers() {
		HashMap<String, List<Tweet>> usersMap = new HashMap<String, List<Tweet>>();
		for (Tweet t : tweets) {
			// only work with lowercase usernames
			String user = t.user.toLowerCase();
			// do we already know of that user???
			if (usersMap.containsKey(user)) {
				// yes, known user: simply add tweet to user's list
				List<Tweet> list = usersMap.get(user);
				list.add(t);
			} else {
				// no, new user: create a new list and add tweet as 1st item
				List<Tweet> list = new ArrayList<Tweet>();
				list.add(t);
				// now associate list with username in map
				usersMap.put(user, list);
			}
		}
		return usersMap;
	}

	/**
	 * Identifies both the oldest and newest item in the pool of tweets loaded
	 * and then returns the time window defined by the two timestamps as
	 * {@link DoubleRange} with the <code>min</code> field of that range
	 * corresponding to the oldest tweet and <code>max</code> to the newest.
	 * 
	 * @return time range
	 */
	public DoubleRange findTimeWindow() {
		long newest = 0;
		long oldest = Long.MAX_VALUE;
		for (Tweet t : tweets) {
			// find oldest tweet
			if (t.date < oldest) {
				oldest = t.date;
			}
			// find newest tweet
			if (t.date > newest) {
				newest = t.date;
			}
		}
		return new DoubleRange(oldest, newest);
	}
}
