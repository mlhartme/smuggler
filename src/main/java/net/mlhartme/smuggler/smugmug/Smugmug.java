/*
 * Copyright Michael Hartmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mlhartme.smuggler.smugmug;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** https://smugmug.atlassian.net/wiki/display/API/Home */
public class Smugmug {
	public static final String API = "https://api.smugmug.com";

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

	public JsonObject getObject(String path, String type) throws IOException {
		return Json.object(get(path), "Response", type);
	}

	public JsonObject get(String path) throws IOException {
		return Json.parse(api(path).get(String.class)).getAsJsonObject();
	}

	public List<JsonObject> getList(String path, String type) throws IOException {
		int i;
		JsonObject obj;
		JsonArray array;
		List<JsonObject> result;
		JsonObject object;

		result = new ArrayList<>();
		i = 0;
		while (true) {
			obj = get(path +"?start=" + (i + 1) + "&count=1");
			array = Json.arrayOpt(Json.object(obj, "Response"), type);
			if (array == null || array.size() == 0) {
				return result;
			} else {
				for (JsonElement e : array) {
					object = e.getAsJsonObject();
					result.add(object);
					i++;
				}
			}
		}
	}

	public WebResource.Builder api(String path) {
		String url;

		if (!path.startsWith("/")) {
			throw new IllegalArgumentException();
		}
		url = API + path;
		if (url.contains("?")) {
			url = url + "&";
		} else {
			url = url + "?";
		}
		url = url + "_pretty=&_verbosity=2";
		return resource(url);
	}

	public WebResource.Builder upload() {
		return resource("http://upload.smugmug.com/");
	}

	private WebResource.Builder resource(String url) {
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

	//--

	public User user(String nickName) {
		return new User(this, "/api/v2/folder/user/" + nickName);
	}

	public Image image(String uri) throws IOException {
		return Image.create(this, getObject(uri, "Image"));
	}

	public Node node(String uri) throws IOException {
		return Node.create(this, getObject(uri, "Node"));
	}

	public Album album(String uri) throws IOException {
		return Album.create(this, getObject(uri, "Album"));
	}

	public Folder folder(String uri) throws IOException {
		return Folder.create(this, getObject(uri, "Folder"));
	}


	public AlbumImage albumImage(String uri) throws IOException {
		return AlbumImage.create(this, getObject(uri, "AlbumImage"));
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
