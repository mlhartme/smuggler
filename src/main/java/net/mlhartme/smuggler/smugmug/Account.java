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
import com.google.gson.JsonPrimitive;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.http.HttpFilesystem;
import net.oneandone.sushi.fs.http.HttpNode;
import net.oneandone.sushi.fs.http.Oauth;
import net.oneandone.sushi.fs.http.model.Method;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/** https://smugmug.atlassian.net/wiki/display/API/Home */
public class Account {
	static {
		new File("target/sushiwire.log").delete();
		HttpFilesystem.wireLog("target/sushiwire.log");
	}

	public static final String API = "https://api.smugmug.com";

	//--

	private final World world;
	private final String consumerKey;
	private final String consumerSecret;
	private final String oauthTokenId;
	private final String oauthTokenSecret;

	public Account(World world, String consumerKey, String consumerSecret, String oauthTokenId, String oauthTokenSecret) {
		this.world = world;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.oauthTokenId = oauthTokenId;
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public void wirelog(PrintStream dest) {
		// TODO
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

	public JsonObject post(String path, String ... keyValues) throws IOException {
		JsonObject obj;
		HttpNode http;
		String str;

		if (!path.startsWith("/")) {
			throw new IllegalArgumentException();
		}
		obj = new JsonObject();
		for (int i = 0; i < keyValues.length; i += 2) {
			obj.add(keyValues[i], new JsonPrimitive(keyValues[i + 1]));
		}

		http = sushi(path);
		http.getRoot().addExtraHeader("Content-Type", "application/json");
		str = http.post(obj.toString() + "\n");
		return Json.parse(str).getAsJsonObject();
	}

	public JsonObject get(String path) throws IOException {
		HttpNode http;
		String str;

		http = sushi(path);
		str = http.readString();
		return Json.parse(str).getAsJsonObject();
	}

	private HttpNode sushi(String path) throws IOException {
		String url;
		HttpNode http;

		url = apiuri(path);
		http = (HttpNode) world.validNode(url);
		http.getRoot().setOauth(new Oauth(consumerKey, consumerSecret, oauthTokenId, oauthTokenSecret));
		http.getRoot().addExtraHeader("Accept", "application/json");
		return http;
	}

	private String apiuri(String path) {
		String url;
		url = API + path;
		if (url.contains("?")) {
			url = url + "&";
		} else {
			url = url + "?";
		}
		url = url + "_pretty=&_verbosity=1";
		return url;
	}

	public void delete(String path) throws IOException {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException();
		}
		Method.delete(sushi(path));
	}

	/**
	 * https://api.smugmug.com/api/v2/doc/reference/upload.html
	 * @return albumImageUri
	 */
	public String upload(net.oneandone.sushi.fs.Node<?> file, String fileName, String uri) throws IOException {
		HttpNode http;
		byte[] image;
		String md5;
		JsonObject response;

		image = file.readBytes();
		md5 = file.md5();
		http = (HttpNode) world.validNode("https://upload.smugmug.com/");
		http.getRoot().setOauth(new Oauth(consumerKey, consumerSecret, oauthTokenId, oauthTokenSecret));
		http.getRoot().addExtraHeader("Content-MD5", md5);
		http.getRoot().addExtraHeader("X-Smug-ResponseType", "JSON");
		http.getRoot().addExtraHeader("X-Smug-FileName", fileName);
		http.getRoot().addExtraHeader("X-Smug-AlbumUri", uri);
		http.getRoot().addExtraHeader("X-Smug-Version", "v2");

		response = Json.parse(http.getWorld().getSettings().string(http.post(image)));
		if (!"ok".equals(Json.string(response, "stat"))) {
			throw new IOException("not ok: " + response);
		}
		return Json.string(response, "Image", "AlbumImageUri");
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
