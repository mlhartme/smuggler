package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;

import java.io.IOException;
import java.util.Properties;

public class Config {
	public static Config load(World world) throws IOException {
		Properties p;

		p = world.getHome().join(".smuggler.properties").readProperties();
		return new Config(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"), get(p, "token.secret"),
				get(p, "user"), get(p, "album"));
	}

	public static String get(Properties p, String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("property not found: " + key);
		}
		return value;
	}

	public final String consumerKey;
	public final String consumerSecret;
	public final String tokenId;
	public final String tokenSecret;
	public final String user;
	public final String album;

	public Config(String consumerKey, String consumerSecret, String tokenId, String tokenSecret, String user, String album) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.tokenId = tokenId;
		this.tokenSecret = tokenSecret;
		this.user = user;
		this.album = album;
	}

	public Smugmug newSmugmug() {
		return new Smugmug(consumerKey, consumerSecret, tokenId, tokenSecret);
	}
}
