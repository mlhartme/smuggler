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

import java.io.IOException;
import java.util.List;

public class AlbumImage extends Handle {
    public static AlbumImage create(Account account, JsonObject ai) {
        return new AlbumImage(account, Json.string(ai, "Uri"), Json.uris(ai, "Image"), Json.string(ai, "FileName"));
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

    public AlbumImage(Account account, String albumImageUri, String imageUri, String fileName) {
        super(account, albumImageUri);
        this.imageUri = imageUri;
        this.fileName = fileName;
    }

    public Album album() throws IOException {
        String albumUri;

        albumUri = uri.substring(0, uri.indexOf("/image"));
        return account.album(albumUri);
    }

    public Image image() throws IOException {
        return account.image(imageUri);
    }
}
