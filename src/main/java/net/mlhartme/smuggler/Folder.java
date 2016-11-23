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
        response = Json.object(smugmug.get("/api/v2/node/" + nodeId + "!children").getAsJsonObject(), "Response");
        if (response.get("Node") != null) {
            array = response.get("Node").getAsJsonArray();
            for (JsonElement e : array) {
                node = e.getAsJsonObject();
                id = Json.string(node, "NodeID");
                type = Json.string(node, "Type");
                switch (type) {
                    case "Folder":
                        result.add(new Folder(Json.string(node, "Uri"), id, Json.string(node, "UrlPath")));
                        break;
                    case "Album":
                        uri = Json.string(node, "Uris", "Album", "Uri");
                        result.add(new Album(id, uri.substring(uri.lastIndexOf('/') + 1), Json.string(node, "Name")));
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
        JsonObject created;

        resource = smugmug.api(uri + "!folders");
        resource.header("Content-Type", "application/json");
        created = Json.object(Json.post(resource, "Name", name, "UrlName", Strings.capitalize(name)), "Response", "Folder");
        return new Folder(Json.string(created, "Uri"), Json.string(created, "NodeID"), Json.string(created, "UrlPath"));
    }

    public Album createAlbum(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject created;

        resource = smugmug.api(uri + "!albums");
        resource.header("Content-Type", "application/json");
        created = Json.object(Json.post(resource, "Name", name, "UrlName", Strings.capitalize(name)), "Response", "Album");
        return new Album(Json.string(created, "NodeID"), Json.string(created, "AlbumKey"), Json.string(created, "Name"));
    }

    public void delete(Smugmug smugmug) {
        WebResource.Builder resource;

        resource = smugmug.api("/api/v2/node/" + nodeId);
        resource.delete();
    }
}
