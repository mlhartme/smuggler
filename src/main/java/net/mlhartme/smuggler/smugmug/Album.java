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

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album extends Handle {
    public static Album create(Account account, JsonObject album) {
        return new Album(account, Json.string(album, "Uri"), Json.string(album, "Name"),
                Json.uris(album, "Node"), Json.uris(album, "Folder"), Json.string(album,"UrlPath"));
    }

    public final String name;
    public final String nodeUri;
    public final String folderUri;
    public final String urlPath;

    public Album(Account account, String uri, String name, String nodeUri, String folderUri, String urlPath) {
        super(account, uri);
        this.name = name;
        this.nodeUri = nodeUri;
        this.folderUri = folderUri;
        this.urlPath = urlPath;
    }

    public Node node() throws IOException {
        return account.node(nodeUri);
    }

    public Folder folder() throws IOException {
        return account.folder(folderUri);
    }

    public List<AlbumImage> listImages() throws IOException {
        List<AlbumImage> result;

        result = new ArrayList<>();
        for (JsonObject object : account.getList(uri + "!images")) {
            result.add(AlbumImage.create(account, object));
        }
        return result;
    }

    public void move(AlbumImage ai) {
        JsonObject result;

        result = account.post(uri + "!moveimages", "MoveUris", ai.uri);
        if (Json.integer(result, "Code") != 200) {
            throw new IllegalStateException(result.toString());
        }
    }

        /** move arguments into this Album */
    public void collect(AlbumImage ai) {
        JsonObject result;

        result = account.post(uri + "!collectimages", "CollectUris", ai.uri);
        if (Json.integer(result, "Code") != 200) {
            throw new IllegalStateException(result.toString());
        }
    }

    /** @return albumImageUri */
    public String upload(net.oneandone.sushi.fs.Node<?> file) throws IOException {
        return upload(file, file.getName());
    }

    /** @return albumImageUri */
    public String upload(net.oneandone.sushi.fs.Node<?> file, String fileName) throws IOException {
        JsonObject response;
        byte[] image;
        String md5;
        WebResource.Builder resource;

        image = file.readBytes();
        resource = account.upload();
        md5 = file.md5();
        resource.header("Content-Length", image.length);
        resource.header("Content-MD5", md5);
        resource.header("X-Smug-ResponseType", "JSON");
        resource.header("X-Smug-FileName", fileName);
        resource.header("X-Smug-AlbumUri", uri);
        resource.header("X-Smug-Version", "v2");

        response = Json.post(resource, image);
        if (!"ok".equals(Json.string(response, "stat"))) {
            throw new IOException("not ok: " + response);
        }
        return Json.string(response, "Image", "AlbumImageUri");
    }
}
