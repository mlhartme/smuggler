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
import java.util.ArrayList;
import java.util.List;

public class User extends Handle {
    public User(Account account, String uri) {
        super(account, uri);
    }

    public Folder folder() throws IOException {
        return Folder.create(account, account.getObject(uri,"Folder"));
    }

    public List<Album> listAlbums() throws IOException {
        List<Album> result;

        result = new ArrayList<>();
        for (JsonObject album : account.getList(uri + "!albums")) {
            result.add(Album.create(account, album));
        }
        return result;
    }

    public Album lookupAlbum(String name) throws IOException {
        for (Album album : listAlbums()) {
            if (album.name.equals(name)) {
                return album;
            }
        }
        return null;
    }
}
