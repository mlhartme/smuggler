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
import java.util.ArrayList;
import java.util.List;

public class User {
    public final String nickName;

    public User(String nickName) {
        this.nickName = nickName;
    }

    public Folder folder(Smugmug smugmug) throws IOException {
        return Folder.create(smugmug, smugmug.getObject("/api/v2/folder/user/" + nickName,"Folder"));
    }

    public List<Album> listAlbums(Smugmug smugmug) throws IOException {
        List<Album> result;

        result = new ArrayList<>();
        for (JsonObject album : smugmug.getList("/api/v2/user/" + nickName + "!albums", "Album")) {
            result.add(Album.create(smugmug, album));
        }
        return result;
    }

    public Album lookupAlbum(Smugmug smugmug, String name) throws IOException {
        for (Album album : listAlbums(smugmug)) {
            if (album.name.equals(name)) {
                return album;
            }
        }
        return null;
    }
}
