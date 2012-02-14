package ciid;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;

import processing.core.PApplet;
import processing.core.PFont;
import toxi.color.TColor;
import toxi.geom.Line2D;
import toxi.geom.Vec2D;
import toxi.util.datatypes.DoubleRange;

public class TweeqViz extends PApplet {

	public static final String SERVER_BASE_URL = "http://localhost:8080";

	public static final String TWEET_BASE_URL = SERVER_BASE_URL + "/tweets/";
	public static final String TWEET_XML_URL = SERVER_BASE_URL + "/xml/tweets";

	// Y position of the base line for user labels
	public static final int HEADER_Y = 100;
	// bleed inset from screen edges
	public static final int BLEED = 10;
	// snap distance (in pixels) for selected tweets
	public static final float SNAP_DIST = 10;

	// color theme definition using different variations of creating TColor
	// objects...
	public static final TColor COL_BG = TColor.newHex("33ddff");
	public static final TColor COL_HIGHLIGHT = TColor.newRGB(1, 0, 1);
	// yellow is 1/6 along the color wheel... (between red and green @ 1/3)
	public static final TColor COL_TWEET = TColor.newHSV(1 / 6f, 1, 1);
	public static final TColor COL_LABEL = TColor.newRGB(1, 1, 1);
	public static final TColor COL_TOOLTIP_BG = TColor.newGrayAlpha(0, 0.8f);
	public static final TColor COL_TOOLTIP_LABEL = TColor.newGray(1);

	// main application entry point...
	// the presence of this function allows us to run as standalone Java
	// application (we just delegate to PApplet though ;)
	public static void main(String[] args) {
		PApplet.main(new String[] { "ciid.TweeqViz" });
	}

	// the wrapper object for all loaded tweets
	private TweetStream stream;
	// an index allowing us to group tweets by username
	private HashMap<String, List<Tweet>> usersMap;
	// an alphabetically sorted list of usernames
	private ArrayList<String> sortedUsers;
	// the currently selected tweet (if any)
	private Tweet selectedTweet = null;

	// fonts (loaded in setup() function)
	private PFont fontBody;
	private PFont fontTitle;

	@Override
	public void draw() {
		// TColors always need to be converted into ARGB first (in Processing)
		background(COL_BG.toARGB());
		// compute column with and initial offset
		float columnWidth = width / usersMap.size();
		float x = columnWidth / 2;
		// iterate over all users and their tweets
		for (String user : sortedUsers) {
			// highlight if user is releated to selected tweet
			if (selectedTweet != null
					&& selectedTweet.user.equalsIgnoreCase(user)) {
				stroke(COL_HIGHLIGHT.toARGB());
				fill(COL_HIGHLIGHT.toARGB());
			} else {
				stroke(COL_LABEL.toARGB());
				fill(COL_LABEL.toARGB());
			}
			textFont(fontTitle);
			// draw user labels
			drawLabel(user, x, HEADER_Y, -45);
			// draw user axis
			drawDashedLine(new Line2D(x, 110, x, height - BLEED), 10);
			noStroke();
			// draw tweets for current user
			for (Tweet t : usersMap.get(user)) {
				drawSingleTweet(t);
			}
			// move along X axis for next user
			x = x + columnWidth;
		}
		// display tooltip if user selected a tweet
		if (selectedTweet != null) {
			drawTweetToolTip(selectedTweet);
		}
	}

	/**
	 * Draws the given line with dashed strokes.
	 * 
	 * @param l
	 *            line
	 * @param dashLen
	 *            length of single dash
	 */
	private void drawDashedLine(Line2D l, float dashLen) {
		// 1st param: result list (or null to create automatically)
		// 2nd param: segment length (distance between points)
		// 3rd param: include first point or not
		List<Vec2D> segments = l.splitIntoSegments(null, dashLen, true);
		for (int i = 0; i < segments.size() - 1; i = i + 2) {
			Vec2D a = segments.get(i);
			Vec2D b = segments.get(i + 1);
			line(a.x, a.y, b.x, b.y);
		}
	}

	/**
	 * Draws a piece of rotated text at the given screen position.
	 * 
	 * @param label
	 *            text
	 * @param x
	 *            screen x pos
	 * @param y
	 *            screen y pos
	 * @param theta
	 *            rotation angle in degrees
	 */
	private void drawLabel(String label, float x, float y, float theta) {
		// backup current (default) coordinate system
		pushMatrix();
		// move origin to where the label should be
		translate(x, y);
		// rotate coord sys
		rotate(radians(theta));
		// draw label from new origin in the rotated coordinate system
		text(label, 0, 0);
		// restore original coordinate sys
		popMatrix();
	}

	/**
	 * Renders a single tweet as dot with different styling if the tweet is
	 * currently the selected one. If so, animates the dot using a sine wave.
	 * 
	 * @param t
	 *            tweet
	 */
	private void drawSingleTweet(Tweet t) {
		float r;
		if (t == selectedTweet) {
			// compute sine wave and re-use value for modulating
			// radius and highlight color
			// remember, sine wave produce outputs in this range:
			// -1.0 ... +1.0
			float modulate = sin(millis() * 0.005f);
			r = modulate * 10 + 15;
			// calling getSaturated() with a negative parameter
			// will desaturate the color by that percentage
			fill(COL_HIGHLIGHT.getSaturated(modulate).toARGB());
		} else {
			fill(COL_TWEET.toARGB());
			r = 10;
		}
		ellipse(t.screenPos.x, t.screenPos.y, r, r);
	}

	/**
	 * Displays the text for the given tweet as tooltip. Attempts to ensure that
	 * text fits on screen.
	 * 
	 * @param t
	 *            tweet
	 */
	private void drawTweetToolTip(Tweet t) {
		// enable small font for tooltip
		textFont(fontBody);
		Vec2D pos = t.screenPos;
		// compute width for given text and current font
		// add some padding
		float tw = textWidth(t.text) + 8;
		// avoid clipping of text on right window edge
		float x = pos.x;
		if (x + tw >= width - BLEED) {
			x = width - BLEED - tw;
		}
		// draw background & label
		fill(COL_TOOLTIP_BG.toARGB());
		rect(x, pos.y - 14, tw, 16);
		fill(COL_TOOLTIP_LABEL.toARGB());
		text(t.text, x + 4, pos.y - 2);
	}

	/**
	 * Attempts to load a list of tweets in XML format from the given url. If
	 * successful, returns result as {@link TweetStream}. If unsuccessful, force
	 * quits application.
	 * 
	 * @param url
	 * @return tweets
	 */
	public TweetStream loadTweets(String url) {
		JAXBContext context;
		// the following operations could fail for various reasons and hence
		// are wrapped within a "try" block, allowing us to gracefully catch
		// some errors and react to them.
		try {
			// create a new XML binding using our TweetStream class
			// JAXB will use the annotations in that class to figure out
			// how the XML data is related to the variables in that class
			context = JAXBContext.newInstance(TweetStream.class);
			// attempt to load & bind tweets from given URL
			TweetStream stream = (TweetStream) context.createUnmarshaller()
					.unmarshal(new URL(url));
			return stream;
		} catch (Exception e) {
			println(e.getMessage());
			// force quit
			System.exit(1);
		}
		return null;
	}

	/**
	 * Analyzes the loaded tweets, builds a map associating each tweet with its
	 * author's username and computes layout coordinates for each tweet.
	 */
	private void mapTweets() {
		usersMap = stream.extractUsers();
		// add the usernames from the map to a new list
		// and sort it alphabetically (the natural order for text strings)
		sortedUsers = new ArrayList<String>(usersMap.keySet());
		Collections.sort(sortedUsers);

		// identify time window of loaded tweets
		DoubleRange timeWindow = stream.findTimeWindow();

		// remaining screen height for time window
		double timeHeight = height - HEADER_Y - 2 * BLEED;
		// length of time window
		double duration = timeWindow.getRange();
		// scale factor time -> screen space
		double ratio = timeHeight / duration;

		// mapping variables
		float columnWidth = width / usersMap.size();
		float x = columnWidth / 2;

		// compute screen positions for all tweets
		for (String user : sortedUsers) {
			for (Tweet t : usersMap.get(user)) {
				float d = (float) ((duration - (t.date - timeWindow.min)) * ratio);
				t.screenPos = new Vec2D(x, d + HEADER_Y + BLEED);
			}
			x = x + columnWidth;
		}
	}

	/**
	 * Executed each time mouse is moved. Checks all tweets if mouse is within
	 * selection radius and if so updates selectedTweet field.
	 * 
	 * @see processing.core.PApplet#mouseMoved()
	 */
	@Override
	public void mouseMoved() {
		selectedTweet = null;
		Vec2D mousePos = new Vec2D(mouseX, mouseY);
		for (Tweet t : stream.tweets) {
			if (t.isRollover(mousePos, SNAP_DIST)) {
				selectedTweet = t;
				return;
			}
		}
	}

	/**
	 * If mouse click occurred whilst mouse is over a tweet, then launches the
	 * web browser with the URL of that tweet.
	 */
	@Override
	public void mousePressed() {
		if (selectedTweet != null) {
			link(TWEET_BASE_URL + selectedTweet.id);
		}
	}

	@Override
	public void setup() {
		size(1024, 720);
		smooth();
		fontBody = loadFont("assets/fonts/droidserif-12.vlw");
		fontTitle = loadFont("assets/fonts/droidserif-bold-14.vlw");
		stream = loadTweets(TWEET_XML_URL);
		mapTweets();

	}
}
