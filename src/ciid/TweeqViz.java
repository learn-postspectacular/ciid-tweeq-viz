package ciid;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;

import processing.core.PApplet;
import toxi.color.TColor;
import toxi.geom.Line2D;
import toxi.geom.Vec2D;

public class TweeqViz extends PApplet {

	public static void main(String[] args) {
		PApplet.main(new String[] { "ciid.TweeqViz" });
	}

	public static final String BASE_URL = "";
	
	TweetStream stream;
	HashMap<String, List<Tweet>> usersMap;

	long newest, oldest;

	Tweet selectedTweet = null;

	public void setup() {
		size(1024, 720);
		smooth();
		stream = loadTweets();
		for (Tweet t : stream.tweets) {
			println(t.text);
		}
		extractUsers();
	}

	private void extractUsers() {
		usersMap = new HashMap<String, List<Tweet>>();
		for (Tweet t : stream.tweets) {
			if (usersMap.containsKey(t.user)) {
				// if known user, add tweet to user's list
				List<Tweet> list = usersMap.get(t.user);
				list.add(t);
				println("user: " + t.user + " size=" + list.size());
			} else {
				List<Tweet> list = new ArrayList<Tweet>();
				list.add(t);
				usersMap.put(t.user, list);
				println("found new user: " + t.user);
			}
		}
		newest = 0;
		oldest = System.currentTimeMillis();

		for (String user : usersMap.keySet()) {
			List<Tweet> userTweets = usersMap.get(user);
			Collections.sort(userTweets);
			for (Tweet t : userTweets) {
				// find oldest tweet
				if (t.date < oldest) {
					oldest = t.date;
				}
				// find newest tweet
				if (t.date > newest) {
					newest = t.date;
				}
			}
		}

	}

	public TweetStream loadTweets() {
		String xmlPath = "http://localhost:8080/xml/tweets";
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(TweetStream.class);
			TweetStream stream = (TweetStream) context.createUnmarshaller()
					.unmarshal(new URL(xmlPath));
			return stream;
		} catch (Exception e) {
			println(e.getMessage());
		}
		return null;
	}

	public void draw() {
		background(0x33ddff);
		// textAlign(CENTER);
		float colWidth = width / (usersMap.size() + 0);
		float x = colWidth / 2;
		for (String user : usersMap.keySet()) {
			// draw user label
			pushMatrix();
			translate(x, 100);
			rotate(-radians(45)); // -PI/4
			fill(255);
			text(user, 0, 0);
			popMatrix();

			stroke(TColor.newRGB(1, 1, 1).toARGB());
			// line(x,110,x,height);

			Line2D l = new Line2D(x, 110, x, height);
			// 1st param: result list (or null to create automatically)
			// 2nd param: segment length (distance between points)
			// 3rd param: include first point or not
			List<Vec2D> segments = l.splitIntoSegments(null, 10, true);
			for (int i = 0; i < segments.size() - 1; i = i + 2) {
				Vec2D a = segments.get(i);
				Vec2D b = segments.get(i + 1);
				line(a.x, a.y, b.x, b.y);
			}

			// remaining height for time window
			float timeHeight = height - 110;
			long duration = newest - oldest;
			// scale factor time -> screen space
			float ratio = timeHeight / duration;

			noStroke();
			for (Tweet t : usersMap.get(user)) {
				float d = (duration - (t.date - oldest)) * ratio;
				if (t == selectedTweet) {
					fill(255, 0, 255);
					float r = sin(frameCount * 0.05f) * 10 + 15;
					ellipse(x, d + 110, r, r);
				} else {
					fill(255, 255, 0);
					ellipse(x, d + 110, 10, 10);
				}
				t.screenPos = new Vec2D(x, d + 110);
			}
			x = x + colWidth;
		}

		if (selectedTweet != null) {
			fill(0);
			text(selectedTweet.text, selectedTweet.screenPos.x,
					selectedTweet.screenPos.y, 160, 80);
		}
	}

	public void mouseMoved() {
		selectedTweet = null;
		Vec2D mousePos = new Vec2D(mouseX, mouseY);
		for (Tweet t : stream.tweets) {
			if (t.isRollover(mousePos)) {
				selectedTweet = t;
			}
		}
	}

	public void mousePressed() {
		if (selectedTweet != null) {
			link("http://localhost:8080/tweets/" + selectedTweet.id);
		}
	}

}
