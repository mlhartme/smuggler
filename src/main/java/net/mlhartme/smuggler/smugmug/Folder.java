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
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder extends Handle {
    public static Folder create(Account account, JsonObject folder) {
        return new Folder(account, Json.string(folder, "Uri") /*Json.uris(folder, "FolderByID")*/,
                Json.string(folder, "Name"), Json.uris(folder, "Node"), Json.string(folder, "UrlPath"));
    }


    public final String name;
    public final String nodeUri;
    public final String urlPath;

    public Folder(Account account, String uri, String name, String nodeUri, String urlPath) {
        super(account, uri);
        this.name = name;
        this.nodeUri = nodeUri;
        this.urlPath = urlPath;
    }

    public Node node() throws IOException {
        return account.node(nodeUri);
    }

    public Folder parent() throws IOException {
        return Folder.create(account, account.getObject(uri + "!parent"));
    }

    public List<Folder> listFolders() throws IOException {
        List<Folder> result;

        result = new ArrayList<>();
        for (JsonObject object : account.getList(uri + "!folders")) {
            result.add(Folder.create(account, object));
        }
        return result;
    }

    public Folder lookupFolder(String name) throws IOException {
        for (Folder folder : listFolders()) {
            if (name.equals(folder.name)) {
                return folder;
            }
        }
        return null;
    }

    public List<Album> listAlbums() throws IOException {
        List<Album> result;

        result = new ArrayList<>();
        for (JsonObject object : account.getList(uri + "!albums")) {
            result.add(Album.create(account, object));
        }
        return result;
    }

    public Album lookupAlbum(String name) throws IOException {
        for (Album album : listAlbums()) {
            if (name.equals(album.name)) {
                return album;
            }
        }
        return null;
    }

    public Folder createFolder(String name) throws IOException {
        JsonObject created;

        created = Json.object(account.post(uri + "!folders", "Name", name, "UrlName", Strings.capitalize(name)), "Response", "Folder");
        return Folder.create(account, created);
    }

    public Album createAlbum(String name) throws IOException {
        JsonObject created;

        created = Json.object(account.post(uri + "!albums",
                "Name", name,
                "UrlName", Strings.capitalize(name),
                "Privacy", "Private",
                "SortMethod", "Date Taken"), "Response", "Album");
        return Album.create(account, created);
    }
}
