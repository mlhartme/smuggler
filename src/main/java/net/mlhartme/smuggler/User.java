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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {
    public final String nickName;

    public User(String nickName) {
        this.nickName = nickName;
    }

    public Folder folder(Smugmug smugmug) throws IOException {
        JsonElement result;
        JsonObject folder;

        result = smugmug.get("/api/v2/folder/user/" + nickName);
        folder = result.getAsJsonObject().get("Response").getAsJsonObject().get("Folder").getAsJsonObject();
        return new Folder(Json.string(folder, "Uri"), Json.string(folder, "NodeID"), Json.string(folder, "UrlPath"));
    }

    public List<Album> listAlbums(Smugmug smugmug) throws IOException {
        JsonObject obj;
        JsonArray array;
        List<Album> result;
        JsonObject sub;

        obj = smugmug.get("/api/v2/user/" + nickName + "!albums").getAsJsonObject();
        array = Json.object(obj, "Response").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            sub = e.getAsJsonObject();
            result.add(new Album(Json.string(sub, "NodeID"), Json.string(sub, "AlbumKey"), Json.string(sub, "Name")));
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
