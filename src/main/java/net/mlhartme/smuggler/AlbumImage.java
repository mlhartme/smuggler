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
import java.util.List;

public class AlbumImage extends Base {
    public static AlbumImage create(JsonObject ai) {
        return new AlbumImage(Json.string(ai, "Uri"), Json.uris(ai, "Image"), Json.string(ai, "FileName"));
    }

    public static AlbumImage lookupFileName(List<AlbumImage> images, String fileName) {
        for (AlbumImage ai : images) {
            if (ai.fileName.equals(fileName)) {
                return ai;
            }
        }
        return null;
    }


    public final String imageUri;
    public final String fileName;

    public AlbumImage(String albumImageUri, String imageUri, String fileName) {
        super(albumImageUri);
        this.imageUri = imageUri;
        this.fileName = fileName;
    }

    public Album album(Smugmug smugmug) throws IOException {
        String albumUri;

        albumUri = uri.substring(0, uri.indexOf("/image"));
        return smugmug.album(albumUri);
    }

    public Image image(Smugmug smugmug) throws IOException {
        return smugmug.image(imageUri);
    }
}
