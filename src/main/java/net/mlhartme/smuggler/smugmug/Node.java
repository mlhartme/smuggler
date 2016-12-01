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

public class Node extends Handle {
    public static Node create(Account smugmug, JsonObject node) {
        String type;
        String folderUri;
        String albumUri;

        type = Json.string(node, "Type");
        switch (type) {
            case "Album":
                folderUri = null;
                albumUri = Json.uris(node, "Album");
                break;
            case "Folder":
                folderUri = Json.uris(node, "FolderByID");
                albumUri = null;
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
        return new Node(smugmug, Json.string(node, "Uri"), Json.uris(node, "ParentNode"), folderUri, albumUri);
    }

    //--

    public final String parentUri;

    /** may be null */
    public final String folderUri;
    /** may be null */
    public final String albumUri;

    public Node(Account smugmug, String uri, String parentUri, String folderUri, String albumUri) {
        super(smugmug, uri);
        this.parentUri = parentUri;
        this.folderUri = folderUri;
        this.albumUri = albumUri;
    }

    public Node parent() throws IOException {
        return smugmug.node(parentUri);
    }

    public boolean isFolder() {
        return folderUri != null;
    }

    public boolean isAlbum() {
        return albumUri != null;
    }

    public Folder folder() throws IOException {
        return smugmug.folder(folderUri);
    }

    public Album album() throws IOException {
        return smugmug.album(albumUri);
    }

    public List<Node> list() throws IOException {
        List<Node> result;

        result = new ArrayList<>();
        for (JsonObject object : smugmug.getList(uri + "!children", "Node")) {
            result.add(Node.create(smugmug, object));
        }
        return result;
    }

}
