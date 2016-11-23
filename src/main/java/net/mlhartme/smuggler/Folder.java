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
package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.WebResource;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder {
    public final String uri;
    public final String nodeId;
    public final String urlPath;

    public Folder(String uri, String nodeId, String urlPath) {
        this.uri = uri;
        this.nodeId = nodeId;
        this.urlPath = urlPath;
    }

    public List<Object> list(Smugmug smugmug) throws IOException {
        JsonObject response;
        List<Object> result;
        JsonArray array;
        String id;
        String type;
        JsonObject node;
        String uri;

        result = new ArrayList<>();
        response = smugmug.get("api/v2/node/" + nodeId + "!children").getAsJsonObject().get("Response").getAsJsonObject();
        if (response.get("Node") != null) {
            array = response.get("Node").getAsJsonArray();
            for (JsonElement e : array) {
                node = e.getAsJsonObject();
                id = node.get("NodeID").getAsString();
                type = node.get("Type").getAsString();
                switch (type) {
                    case "Folder":
                        result.add(new Folder(node.get("Uri").getAsString(), id, node.get("UrlPath").getAsString()));
                        break;
                    case "Album":
                        uri = node.get("Uris").getAsJsonObject().get("Album").getAsJsonObject().get("Uri").getAsString();
                        result.add(new Album(id, uri.substring(uri.lastIndexOf('/') + 1), node.get("Name").getAsString()));
                        break;
                    default:
                        throw new IOException("unexpected type: " + type);
                }
            }
        }

        return result;
    }

    public Folder createFolder(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject obj;
        JsonObject created;
        String response;

        resource = smugmug.resource("https://api.smugmug.com" + uri + "!folders");
        resource.header("Content-Type", "application/json");
        obj = new JsonObject();
        obj.add("Name", new JsonPrimitive(name));
        obj.add("UrlName", new JsonPrimitive(Strings.capitalize(name)));
       // obj.add("Privacy", new JsonPrimitive("Public"));
        response = resource.post(String.class, obj.toString());
        created = new JsonParser().parse(response).getAsJsonObject().get("Response").getAsJsonObject().get("Folder").getAsJsonObject();
        return new Folder(created.get("Uri").getAsString(), created.get("NodeID").getAsString(), created.get("UrlPath").getAsString());
    }

    public Album createAlbum(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject obj;
        String response;
        JsonObject created;

        resource = smugmug.resource("https://api.smugmug.com" + uri + "!albums");
        resource.header("Content-Type", "application/json");
        obj = new JsonObject();
        obj.add("Name", new JsonPrimitive(name));
        obj.add("UrlName", new JsonPrimitive(Strings.capitalize(name)));
        // obj.add("Privacy", new JsonPrimitive("Public"));
        response = resource.post(String.class, obj.toString());
        created = new JsonParser().parse(response).getAsJsonObject().get("Response").getAsJsonObject().get("Album").getAsJsonObject();
        return new Album(created.get("NodeID").getAsString(), created.get("AlbumKey").getAsString(), created.get("Name").getAsString());
    }

    public void delete(Smugmug smugmug) {
        WebResource.Builder resource;

        resource = smugmug.resource("https://api.smugmug.com/api/v2/node/" + nodeId);
        resource.delete();
    }
}
