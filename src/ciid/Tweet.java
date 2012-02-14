package ciid;

import javax.xml.bind.annotation.XmlElement;

import toxi.geom.Vec2D;

public class Tweet implements Comparable<Tweet> {

	@XmlElement
	long date;

	@XmlElement
	String text;

	@XmlElement
	String user;

	@XmlElement
	String id;

	Vec2D screenPos;

	@Override
	public int compareTo(Tweet t) {
		if (date < t.date) {
			return -1;
		}
		if (date == t.date) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Checks if the given point p is within the circle of snapDist units around
	 * the tweet's screen position.
	 * 
	 * @param p
	 * @param snapDist
	 * @return true, if inside.
	 */
	public boolean isRollover(Vec2D p, float snapDist) {
		return p.distanceTo(screenPos) < snapDist;
	}

}