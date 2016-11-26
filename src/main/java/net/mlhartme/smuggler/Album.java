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
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album {
    public static Album create(JsonObject album) {
        return new Album(Json.string(album, "NodeID"), Json.string(album, "AlbumKey"), Json.string(album, "Name"));
    }

    public static Object fromNode(JsonObject node) {
        String uri;

        uri = Json.string(node, "Uris", "Album", "Uri");
        return new Album(Json.string(node, "Type"),
                uri.substring(uri.lastIndexOf('/') + 1), Json.string(node, "Name"));
    }

    public final String nodeId;
    public final String key;
    public final String name;

    public Album(String nodeId, String key, String name) {
        this.nodeId = nodeId;
        this.key = key;
        this.name = name;
    }

    public List<Image> list(Smugmug smugmug) throws IOException {
        JsonObject obj;
        JsonArray array;
        List<Image> result;
        JsonObject object;

        obj = smugmug.get("/api/v2/album/" + key + "!images");
        array = Json.element(Json.object(obj, "Response"), "AlbumImage").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            object = e.getAsJsonObject();
            result.add(new Image(Json.string(object, "ImageKey"), Json.string(object, "FileName")));
        }
        return result;
    }

    /** @return albumImageUri */
    public String upload(Smugmug smugmug, FileNode file) throws IOException {
        JsonObject response;
        byte[] image;
        String md5;
        WebResource.Builder resource;

        image = file.readBytes();
        resource = smugmug.upload();
        md5 = file.md5();
        resource.header("Content-Length", image.length);
        resource.header("Content-MD5", md5);
        resource.header("X-Smug-ResponseType", "JSON");
        resource.header("X-Smug-FileName", file.getName());
        resource.header("X-Smug-AlbumUri", "/api/v2/album/" + key);
        resource.header("X-Smug-Version", "v2");

        response = Json.post(resource, image);
        if (!"ok".equals(Json.string(response, "stat"))) {
            throw new IOException("not ok: " + response);
        }
        return Json.string(response, "Image", "AlbumImageUri");
    }

    public void delete(Smugmug smugmug) {
        WebResource.Builder resource;

        resource = smugmug.api("/api/v2/node/" + nodeId);
        resource.delete();
    }
}
