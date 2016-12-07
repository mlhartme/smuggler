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
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.*;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.http.HttpNode;
import net.oneandone.sushi.fs.http.Oauth;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/** https://smugmug.atlassian.net/wiki/display/API/Home */
public class Account {
	public static final String API = "https://api.smugmug.com";

	//--

	private final World world;
	private final Client client;
	private final String consumerKey;
	private final String consumerSecret;
	private final String oauthTokenId;
	private final String oauthTokenSecret;

	public Account(World world, String consumerKey, String consumerSecret, String oauthTokenId, String oauthTokenSecret) {
		this.world = world;
		this.client = Client.create();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.oauthTokenId = oauthTokenId;
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public void wirelog(PrintStream dest) {
		client.addFilter(new LoggingFilter(dest) {
			@Override
			public ClientResponse handle(ClientRequest request) {
				ClientResponse response;

				if (request.getURI().getHost().contains("upload")) {
					dest.println("> upload " + request.getURI());
					response = this.getNext().handle(request);
					dest.println("< " + response.getStatus());
					return response;
				} else {
					return super.handle(request);
				}
			}

		});
	}

	public JsonObject getObject(String path) throws IOException {
		JsonObject response;
		String locator;

		response = Json.object(get(path), "Response");
		locator = Json.string(response, "Locator");
		return Json.object(response, locator);
	}

	public List<JsonObject> getList(String path) throws IOException {
		int i;
		String locator;
		JsonObject obj;
		JsonArray array;
		List<JsonObject> result;
		JsonObject object;

		result = new ArrayList<>();
		i = 0;
		while (true) {
			obj = get(path +"?start=" + (i + 1) + "&count=100");
			locator = Json.string(obj, "Response","Locator");
			array = Json.arrayOpt(Json.object(obj, "Response"), locator);
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

	public JsonObject post(String path, String ... keyValues) {
		return Json.post(api(path), keyValues);
	}

	public JsonObject get(String path) throws IOException {
		String url;
		HttpNode http;
		String str;

		url = API + path;
		if (url.contains("?")) {
			url = url + "&";
		} else {
			url = url + "?";
		}
		url = url + "_pretty=&_verbosity=1";
		http = (HttpNode) world.validNode(url);
		http.getRoot().setOauth(new Oauth(consumerKey, consumerSecret, oauthTokenId, oauthTokenSecret));
		http.getRoot().addExtraHeader("Accept", "application/json");
		str = http.readString();
		return Json.parse(str).getAsJsonObject();
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
		url = url + "_pretty=&_verbosity=1";
		return resource(url);
	}

	public WebResource.Builder upload() {
		return resource("http://upload.smugmug.com/");
	}

	private WebResource.Builder resource(String url) {
		OAuthSecrets secrets;
		OAuthParameters params;
		WebResource resource;
		WebResource.Builder result;

		resource = client.resource(url);
		secrets = new OAuthSecrets().consumerSecret(consumerSecret);
		secrets.setTokenSecret(oauthTokenSecret);
		params = new OAuthParameters().consumerKey(consumerKey).signatureMethod("HMAC-SHA1").version("1.0");
		params.token(oauthTokenId);
		resource.addFilter(new OAuthClientFilter(client.getProviders(), params, secrets));
		result = resource.accept("application/json");
		return result;
	}

	//--

	public User user(String nickName) {
		return new User(this, "/api/v2/folder/user/" + nickName);
	}

	public Image image(String uri) throws IOException {
		return Image.create(this, getObject(uri));
	}

	public Node node(String uri) throws IOException {
		return Node.create(this, getObject(uri));
	}

	public Album album(String uri) throws IOException {
		return Album.create(this, getObject(uri));
	}

	public Folder folder(String uri) throws IOException {
		return Folder.create(this, getObject(uri));
	}


	public AlbumImage albumImage(String uri) throws IOException {
		return AlbumImage.create(this, getObject(uri));
	}

}
