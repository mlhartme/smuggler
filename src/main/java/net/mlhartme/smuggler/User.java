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

        result = smugmug.get("api/v2/folder/user/" + nickName);
        folder = result.getAsJsonObject().get("Response").getAsJsonObject().get("Folder").getAsJsonObject();
        return new Folder(folder.get("Uri").getAsString(),
                folder.get("NodeID").getAsString(), folder.get("UrlPath").getAsString());
    }

    public List<Album> listAlbums(Smugmug smugmug) throws IOException {
        JsonObject obj;
        JsonArray array;
        List<Album> result;

        obj = smugmug.get("api/v2/user/" + nickName + "!albums").getAsJsonObject();
        array = obj.get("Response").getAsJsonObject().get("Album").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            result.add(new Album(e.getAsJsonObject().get("NodeID").getAsString(), e.getAsJsonObject().get("AlbumKey").getAsString(), e.getAsJsonObject().get("Name").getAsString()));
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
