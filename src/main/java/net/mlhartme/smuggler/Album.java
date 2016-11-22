package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.WebResource;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album {
    public final String nodeId;
    public final String key;
    public final String name;

    public Album(String nodeId, String key, String name) {
        this.nodeId = nodeId;
        this.key = key;
        this.name = name;
    }

    public List<Image> list(Smugmug smugmug) throws IOException {
        JsonObject obj;
        JsonArray array;
        List<Image> result;
        JsonObject object;

        obj = smugmug.get("api/v2/album/" + key + "!images").getAsJsonObject();
        array = obj.get("Response").getAsJsonObject().get("AlbumImage").getAsJsonArray();
        result = new ArrayList<>();
        for (JsonElement e : array) {
            object = e.getAsJsonObject();
            result.add(new Image(object.get("ImageKey").getAsString(), object.get("FileName").getAsString()));
        }
        return result;
    }

    public Image upload(Smugmug smugmug, FileNode file) throws IOException {
        JsonObject response;
        byte[] image;
        String md5;
        WebResource.Builder builder;
        String uri;
        int idx;

        image = file.readBytes();
        builder = smugmug.resource("http://upload.smugmug.com/");
        md5 = file.md5();
        builder = builder.header("Content-Length", image.length);
        builder = builder.header("Content-MD5", md5);
        builder = builder.header("X-Smug-ResponseType", "JSON");
        builder = builder.header("X-Smug-FileName", file.getName());
        builder = builder.header("X-Smug-AlbumUri", "/api/v2/album/" + key);
        builder = builder.header("X-Smug-Version", "v2");

        response = new JsonParser().parse(builder.post(String.class, image)).getAsJsonObject();
        if (!"ok".equals(response.get("stat").getAsString())) {
            throw new IOException("not ok: " + response);
        }
        uri = response.get("Image").getAsJsonObject().get("ImageUri").getAsString();
        idx = uri.lastIndexOf('/');
        return new Image(uri.substring(idx + 1), file.getName());
    }

    public void delete(Smugmug smugmug) {
        WebResource.Builder resource;

        resource = smugmug.resource("https://api.smugmug.com/api/v2/node/" + nodeId);
        resource.delete();
    }
}
