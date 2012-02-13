package ciid;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="tweets")
public class TweetStream {

	@XmlElement(name="tweet")
	List<Tweet> tweets = new ArrayList<Tweet>();
}
