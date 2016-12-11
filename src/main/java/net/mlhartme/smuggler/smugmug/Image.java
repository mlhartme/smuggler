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

public class Image extends Handle {
    public static Image create(Account account, JsonObject image) {
        return new Image(account, Json.string(image, "Uri"), Json.string(image, "FileName"), Json.string(image, "ArchivedMD5"),
                Json.uris(image, "ImageAlbum"));
    }

    //--

    public final String fileName;
    public final String md5;
    public final String album; // TODO: becomes out-dated when album image is moved

    public Image(Account account, String uri, String fileName, String md5, String album) {
        super(account, uri);
        this.fileName = fileName;
        this.md5 = md5;
        this.album = album;
    }

    public Folder album() throws IOException {
        return account.folder(album);
    }

    public void setTitle(String title) throws IOException {
        account.patch(uri, "Title", title);
    }
}
