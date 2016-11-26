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

import java.io.IOException;

public class AlbumImage {
    public static AlbumImage create(JsonObject ai) {
        return new AlbumImage(Json.string(ai, "Uri"), Json.string(ai, "Uris", "Image", "Uri"));
    }

    public final String albumImageUri;
    public final String imageUri;

    public AlbumImage(String albumImageUri, String imageUri) {
        this.albumImageUri = albumImageUri;
        this.imageUri = imageUri;
    }

    public void delete(Smugmug smugmug) {
        smugmug.api(albumImageUri).delete();
    }

    public Album album(Smugmug smugmug) throws IOException {
        String albumUri;

        albumUri = albumImageUri.substring(0, albumImageUri.indexOf("/image"));
        return smugmug.album(albumUri);
    }

    public Image image(Smugmug smugmug) throws IOException {
        return smugmug.image(imageUri);
    }
}
