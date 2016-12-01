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

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.WebResource;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album extends Base {
    public static Album create(JsonObject album) {
        return new Album(Json.string(album, "Uri"), Json.string(album, "Name"), Json.uris(album, "Node"));
    }

    public final String name;
    public final String nodeUri;

    public Album(String uri, String name, String nodeUri) {
        super(uri);
        this.name = name;
        this.nodeUri = nodeUri;
    }

    public List<AlbumImage> listImages(Smugmug smugmug) throws IOException {
        List<AlbumImage> result;

        result = new ArrayList<>();
        for (JsonObject object : smugmug.getList(uri + "!images", "AlbumImage")) {
            result.add(AlbumImage.create(object));
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
        resource.header("X-Smug-AlbumUri", uri);
        resource.header("X-Smug-Version", "v2");

        response = Json.post(resource, image);
        if (!"ok".equals(Json.string(response, "stat"))) {
            throw new IOException("not ok: " + response);
        }
        return Json.string(response, "Image", "AlbumImageUri");
    }
}
