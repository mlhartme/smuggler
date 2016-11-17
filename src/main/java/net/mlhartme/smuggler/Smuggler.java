package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Smuggler {
	public static Smuggler load() throws IOException {
		Properties p = new Properties();
		try (Reader src = new FileReader("/Users/mhm/.smuggler.properties") ){
			p.load(src);
		}
		return new Smuggler(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"),
				get(p, "token.secret"), get(p, "album"));
	}

	private static String get(Properties p, String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException(key);
		}
		return value;
	}

	//--

	private final Client client;
	private final String consumerKey;
	private final String consumerSecret;
	private final String oauthTokenId;
	private final String oauthTokenSecret;
	public final String album;

	public Smuggler(String consumerKey, String consumerSecret, String oauthTokenId, String oauthTokenSecret, String album) {
		this.client = Client.create();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.oauthTokenId = oauthTokenId;
		this.oauthTokenSecret = oauthTokenSecret;
		this.album = album;
	}

	public List<String> album(String album) throws IOException {
		JsonObject obj;
		JsonArray array;
		List<String> result;

		obj = request("api/v2/album/" + album + "!images").getAsJsonObject();
		array = obj.get("Response").getAsJsonObject().get("AlbumImage").getAsJsonArray();
		result = new ArrayList<>();
		for (JsonElement e : array) {
			result.add(e.getAsJsonObject().get("FileName").getAsString());
		}
		return result;
	}

	public JsonElement request(String path) throws IOException {
		OAuthSecrets secrets;
		OAuthParameters params;

		System.out.println("test");

		WebResource resource = client.resource("https://api.smugmug.com/" + path);
		secrets = new OAuthSecrets().consumerSecret(consumerSecret);
		secrets.setTokenSecret(oauthTokenSecret);
		params = new OAuthParameters().consumerKey(consumerKey).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(oauthTokenId);
		resource.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));

		WebResource.Builder builder = resource.accept("application/json");
		return new JsonParser().parse(builder.get(String.class));
	}
}
