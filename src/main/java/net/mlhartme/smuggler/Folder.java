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
import com.sun.jersey.api.client.WebResource;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder extends Base {
    public static Folder fromNode(JsonObject node) {
        return new Folder(Json.string(node, "Uri"), Json.string(node, "Name"), Json.string(node, "NodeID"), Json.string(node, "UrlPath"), "TODO");
    }

    public static Folder create(JsonObject folder) {
        return new Folder(Json.string(folder, "Uri"), Json.string(folder, "Name"), Json.string(folder, "NodeID"), Json.string(folder, "UrlPath"), "TODO"/*
                Json.string(folder, "Uris", "ParentFolder", "Uri")*/);
    }


    public final String name;
    public final String nodeId;
    public final String urlPath;
    public final String parentUri;

    public Folder(String uri, String name, String nodeId, String urlPath, String parentUri) {
        super(uri);
        this.name = name;
        this.nodeId = nodeId;
        this.urlPath = urlPath;
        this.parentUri = parentUri;
    }

    public Folder parentFolder(Smugmug smugmug) throws IOException {
        return smugmug.folder(uri);
    }

    public List<Folder> listFolders(Smugmug smugmug) throws IOException {
        List<Folder> result;

        result = new ArrayList<>();
        for (JsonObject object : smugmug.getList(uri + "!folders", "Folder")) {
            result.add(Folder.create(object));
        }
        return result;
    }

    public List<Object> list(Smugmug smugmug) throws IOException {
        JsonObject response;
        List<Object> result;
        JsonArray array;
        String type;
        JsonObject node;

        result = new ArrayList<>();
        response = Json.object(smugmug.get("/api/v2/node/" + nodeId + "!children"), "Response");
        if (response.get("Node") != null) {
            array = response.get("Node").getAsJsonArray();
            for (JsonElement e : array) {
                node = e.getAsJsonObject();
                type = Json.string(node, "Type");
                switch (type) {
                    case "Folder":
                        result.add(Folder.fromNode(node));
                        break;
                    case "Album":
                        result.add(Album.fromNode(node));
                        break;
                    default:
                        throw new IOException("unexpected type: " + type);
                }
            }
        }

        return result;
    }

    public Folder lookupFolder(Smugmug smugmug, String name) throws IOException {
        Folder folder;

        for (Object obj : list(smugmug)) {
            if (obj instanceof Folder) {
                folder = (Folder) obj;
                if (name.equals(folder.name)) {
                    return folder;
                }
            }
        }
        return null;
    }

    public Folder createFolder(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject created;

        resource = smugmug.api(uri + "!folders");
        resource.header("Content-Type", "application/json");
        created = Json.object(Json.post(resource, "Name", name, "UrlName", Strings.capitalize(name)), "Response", "Folder");
        return Folder.create(created);
    }

    public Album createAlbum(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject created;

        resource = smugmug.api(uri + "!albums");
        resource.header("Content-Type", "application/json");
        created = Json.object(Json.post(resource, "Name", name, "UrlName", Strings.capitalize(name)), "Response", "Album");
        return Album.create(created);
    }
}
