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
import com.sun.jersey.api.client.WebResource;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album {
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

        obj = smugmug.get("/api/v2/album/" + key + "!images").getAsJsonObject();
        array = Json.element(Json.object(obj, "Response"), "AlbumImage").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            object = e.getAsJsonObject();
            result.add(new Image(Json.string(object, "ImageKey"), Json.string(object, "FileName")));
        }
        return result;
    }

    public Image upload(Smugmug smugmug, FileNode file) throws IOException {
        JsonObject response;
        byte[] image;
        String md5;
        WebResource.Builder builder;
        String uri;
        int idx;

        image = file.readBytes();
        builder = smugmug.upload();
        md5 = file.md5();
        builder = builder.header("Content-Length", image.length);
        builder = builder.header("Content-MD5", md5);
        builder = builder.header("X-Smug-ResponseType", "JSON");
        builder = builder.header("X-Smug-FileName", file.getName());
        builder = builder.header("X-Smug-AlbumUri", "/api/v2/album/" + key);
        builder = builder.header("X-Smug-Version", "v2");

        response = new JsonParser().parse(builder.post(String.class, image)).getAsJsonObject();
        if (!"ok".equals(Json.string(response, "stat"))) {
            throw new IOException("not ok: " + response);
        }
        uri = Json.string(response, "Image", "ImageUri");
        idx = uri.lastIndexOf('/');
        return new Image(uri.substring(idx + 1), file.getName());
    }

    public void delete(Smugmug smugmug) {
        WebResource.Builder resource;

        resource = smugmug.api("/api/v2/node/" + nodeId);
        resource.delete();
    }
}
