package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** https://smugmug.atlassian.net/wiki/display/API/Home */
public class Smuggler {
	public static Smuggler load() throws IOException {
		Properties p = new Properties();
		try (Reader src = new FileReader("/Users/mhm/.smuggler.properties") ){
			p.load(src);
		}
		return new Smuggler(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"),
				get(p, "token.secret"), get(p, "user"), get(p, "album"));
	}

	private static String get(Properties p, String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("property not found: " + key);
		}
		return value;
	}

	//--

	private final Client client;
	private final String consumerKey;
	private final String consumerSecret;
	private final String oauthTokenId;
	private final String oauthTokenSecret;

	public final String user;
	public final String album;

	public Smuggler(String consumerKey, String consumerSecret, String oauthTokenId, String oauthTokenSecret, String user, String album) {
		this.client = Client.create();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.oauthTokenId = oauthTokenId;
		this.oauthTokenSecret = oauthTokenSecret;
		this.user = user;
		this.album = album;
	}

	public void wirelog(PrintStream dest) {
		client.addFilter(new LoggingFilter(dest));
	}

	public String upload(FileNode file, String album) throws IOException {
		JsonObject response;
		byte[] image = file.readBytes();
		WebResource resource = resource("http://upload.smugmug.com/");

		String md5 = file.md5();

		WebResource.Builder builder = resource.getRequestBuilder();
		builder = builder.header("Content-Length", image.length);
		builder = builder.header("Content-MD5", md5);
		builder = builder.header("X-Smug-ResponseType", "JSON");
		builder = builder.header("X-Smug-FileName", file.getName());
		builder = builder.header("X-Smug-AlbumUri", "/api/v2/album/" + album);
		builder = builder.header("X-Smug-Version", "v2");

		response = new JsonParser().parse(builder.post(String.class, image)).getAsJsonObject();
		if (!"ok".equals(response.get("stat").getAsString())) {
			throw new IOException("not ok: " + response);
		}
		return response.get("Image").getAsJsonObject().get("ImageUri").getAsString();
	}

	public Map<String, String> listAlbums(String user) throws IOException {
		JsonObject obj;
		JsonArray array;
		Map<String, String> result;

		obj = request("api/v2/user/" + user + "!albums").getAsJsonObject();
		array = obj.get("Response").getAsJsonObject().get("Album").getAsJsonArray();
		result = new HashMap<>();
		for (JsonElement e : array) {
			result.put(e.getAsJsonObject().get("Name").getAsString(), e.getAsJsonObject().get("AlbumKey").getAsString());
		}
		return result;
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

	private JsonElement request(String path) throws IOException {
		WebResource resource = resource("https://api.smugmug.com/" + path);
		WebResource.Builder builder = resource.accept("application/json");
		return new JsonParser().parse(builder.get(String.class));
	}

	private WebResource resource(String url) {
		OAuthSecrets secrets;
		OAuthParameters params;
		WebResource result;

		result = client.resource(url);
		secrets = new OAuthSecrets().consumerSecret(consumerSecret);
		secrets.setTokenSecret(oauthTokenSecret);
		params = new OAuthParameters().consumerKey(consumerKey).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(oauthTokenId);
		result.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));
		return result;
	}
}
