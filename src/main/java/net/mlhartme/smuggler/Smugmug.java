package net.mlhartme.smuggler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

/** https://smugmug.atlassian.net/wiki/display/API/Home */
public class Smugmug {
	//--

	private final Client client;
	private final String consumerKey;
	private final String consumerSecret;
	private final String oauthTokenId;
	private final String oauthTokenSecret;

	public Smugmug(String consumerKey, String consumerSecret, String oauthTokenId, String oauthTokenSecret) {
		this.client = Client.create();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.oauthTokenId = oauthTokenId;
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public void wirelog(PrintStream dest) {
		client.addFilter(new LoggingFilter(dest));
	}

	public User user(String id) {
		return new User(id);
	}

	public JsonElement get(String path) throws IOException {
		WebResource.Builder builder;

		builder = resource("https://api.smugmug.com/" + path);
		return new JsonParser().parse(builder.get(String.class));
	}

	public WebResource.Builder resource(String url) {
		OAuthSecrets secrets;
		OAuthParameters params;
		WebResource resource;

		resource = client.resource(url);
		secrets = new OAuthSecrets().consumerSecret(consumerSecret);
		secrets.setTokenSecret(oauthTokenSecret);
		params = new OAuthParameters().consumerKey(consumerKey).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(oauthTokenId);
		resource.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));
		return resource.accept("application/json");
	}

	//-- TODO

	public void addOauth(WebResource.Builder dest) {
		StringBuilder builder;

		builder = new StringBuilder();
		arg(builder, "OAuth oauth_nonce", UUID.randomUUID().toString());
		arg(builder, "oauth_token", oauthTokenId);
		arg(builder, "oauth_consumer_key", consumerKey);
		arg(builder, "oauth_signature_method", "HMAC-SHA1");
		arg(builder, "oauth_version", "1.0");
		arg(builder, "oauth_timestamp", new Long(System.currentTimeMillis() / 1000).toString());
		arg(builder, "oauth_signature", signature());

		dest.header("Authorization", builder.toString());
	}

	private static String signature() {
		return "";
	}

	private static void arg(StringBuilder builder, String key, String value) {
		if (builder.length() > 0) {
			builder.append(", ");
		}
		builder.append(key);
		builder.append('=');
		builder.append('"');
		builder.append(value);
		builder.append('"');
	}
}
