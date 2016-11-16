package net.mlhartme.smuggler;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

	private static String CONSUMER_KEY = get("consumer.key");
	private static String CONSUMER_SECRET = get("consumer.secret");
	private static String OAUTH_TOKEN_ID = get("token.id");
	private static String OAUTH_TOKEN_SECRET = get("token.secret");

	private static String get(String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException(key);
		}
		return value;
	}

	public static void main(String[] args) throws IOException {
		OAuthSecrets secrets;
		OAuthParameters params;

		System.out.println("test");

		WebResource resource = CLIENT.resource("https://api.smugmug.com/api/v2/user/mlhartme");
		secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
		secrets.setTokenSecret(OAUTH_TOKEN_SECRET);
		params = new OAuthParameters().consumerKey(CONSUMER_KEY).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(OAUTH_TOKEN_ID);
		resource.addFilter(new OAuthClientFilter(CLIENT.getProviders(), params, secrets));

		WebResource.Builder builder = resource.accept("application/json");
		System.out.println(builder.get(String.class));
	}
}
