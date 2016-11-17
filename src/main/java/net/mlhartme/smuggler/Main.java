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

public class Main {
	public static Client CLIENT = Client.create();

	private static final Properties p = new Properties();
	static {
		try {
			try (Reader src = new FileReader("/Users/mhm/.smuggler.properties") ){
				p.load(src);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final String CONSUMER_KEY = get("consumer.key");
	private static final String CONSUMER_SECRET = get("consumer.secret");
	private static final String OAUTH_TOKEN_ID = get("token.id");
	private static final String OAUTH_TOKEN_SECRET = get("token.secret");

	private static final String ALBUM = get("album");

	private static String get(String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException(key);
		}
		return value;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(album(ALBUM).toString());

	}

	public static List<String> album(String album) throws IOException {
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

	public static JsonElement request(String path) throws IOException {
		OAuthSecrets secrets;
		OAuthParameters params;

		System.out.println("test");

		WebResource resource = CLIENT.resource("https://api.smugmug.com/" + path);
		secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
		secrets.setTokenSecret(OAUTH_TOKEN_SECRET);
		params = new OAuthParameters().consumerKey(CONSUMER_KEY).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(OAUTH_TOKEN_ID);
		resource.addFilter(new OAuthClientFilter(CLIENT.getProviders(), params, secrets));

		WebResource.Builder builder = resource.accept("application/json");
		return new JsonParser().parse(builder.get(String.class));
	}
}
