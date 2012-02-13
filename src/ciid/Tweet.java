package ciid;

import javax.xml.bind.annotation.XmlElement;

import toxi.geom.ReadonlyVec2D;
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

	public boolean isRollover(Vec2D mousePos) {
		return mousePos.distanceTo(screenPos) < 10;
	}

}