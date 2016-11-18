package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder {
    public final String nodeId;
    public final String urlPath;

    public Folder(String nodeId, String urlPath) {
        this.nodeId = nodeId;
        this.urlPath = urlPath;
    }

    public List<Object> list(Smugmug smugmug) throws IOException {
        List<Object> result;
        JsonArray array;
        String id;
        String type;
        JsonObject node;
        String uri;

        result = new ArrayList<>();
        array = smugmug.get("api/v2/node/" + nodeId + "!children").getAsJsonObject().get("Response").getAsJsonObject().get("Node").getAsJsonArray();
        for (JsonElement e : array) {
            node = e.getAsJsonObject();
            id = node.get("NodeID").getAsString();
            type = node.get("Type").getAsString();
            switch (type) {
                case "Folder":
                    result.add(new Folder(id, node.get("UrlPath").getAsString()));
                    break;
                case "Album":
                    uri = node.get("Uris").getAsJsonObject().get("Album").getAsJsonObject().get("Uri").getAsString();
                    result.add(new Album(id, uri.substring(uri.lastIndexOf('/') + 1), node.get("Name").getAsString()));
                    break;
                default:
                    throw new IOException("unexpected type: " + type);
            }
        }
        return result;
    }
}
