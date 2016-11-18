package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {
    public final String key;

    public User(String key) {
        this.key = key;
    }

    public List<Album> listAlbums(Smugmug smugmug) throws IOException {
        JsonObject obj;
        JsonArray array;
        List<Album> result;

        obj = smugmug.request("api/v2/user/" + key + "!albums").getAsJsonObject();
        array = obj.get("Response").getAsJsonObject().get("Album").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            result.add(new Album(e.getAsJsonObject().get("AlbumKey").getAsString(), e.getAsJsonObject().get("Name").getAsString()));
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
