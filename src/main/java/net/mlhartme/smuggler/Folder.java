package net.mlhartme.smuggler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder {
    public final String uri;
    public final String nodeId;
    public final String urlPath;

    public Folder(String uri, String nodeId, String urlPath) {
        this.uri = uri;
        this.nodeId = nodeId;
        this.urlPath = urlPath;
    }

    public List<Object> list(Smugmug smugmug) throws IOException {
        JsonObject response;
        List<Object> result;
        JsonArray array;
        String id;
        String type;
        JsonObject node;
        String uri;

        result = new ArrayList<>();
        response = smugmug.get("api/v2/node/" + nodeId + "!children").getAsJsonObject().get("Response").getAsJsonObject();
        if (response.get("Node") != null) {
            array = response.get("Node").getAsJsonArray();
            for (JsonElement e : array) {
                node = e.getAsJsonObject();
                id = node.get("NodeID").getAsString();
                type = node.get("Type").getAsString();
                switch (type) {
                    case "Folder":
                        result.add(new Folder(node.get("Uri").getAsString(), id, node.get("UrlPath").getAsString()));
                        break;
                    case "Album":
                        uri = node.get("Uris").getAsJsonObject().get("Album").getAsJsonObject().get("Uri").getAsString();
                        result.add(new Album(id, uri.substring(uri.lastIndexOf('/') + 1), node.get("Name").getAsString()));
                        break;
                    default:
                        throw new IOException("unexpected type: " + type);
                }
            }
        }

        return result;
    }

    public void createFolder(Smugmug smugmug, String name) {
        WebResource.Builder resource;
        JsonObject obj;

        resource = smugmug.resource("https://api.smugmug.com" + uri + "!folders");
        resource.header("Content-Type", "application/json");
        obj = new JsonObject();
        obj.add("Name", new JsonPrimitive(name));
        obj.add("UrlName", new JsonPrimitive(name));
       // obj.add("Privacy", new JsonPrimitive("Public"));
        resource.post(obj.toString());
    }

    public void createAlbum(Smugmug smugmug, String foo) {
        WebResource.Builder resource;
        JsonObject obj;

        resource = smugmug.resource("https://api.smugmug.com" + uri + "!albums?_verbosity=1&Name=" + foo + "&UrlName=" + foo);
        resource.header("Content-Type", "application/json");
        obj = new JsonObject();
        obj.add("Name", new JsonPrimitive(foo));
        obj.add("UrlName", new JsonPrimitive(foo));
        // obj.add("Privacy", new JsonPrimitive("Public"));
        resource.post(obj.toString());
    }
}
