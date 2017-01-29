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
package net.mlhartme.smuggler.cli;

import net.mlhartme.smuggler.cache.AlbumData;
import net.mlhartme.smuggler.cache.FolderData;
import net.mlhartme.smuggler.cache.ImageData;
import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sync extends Command {
    public Sync() throws IOException {
    }

    public void run(User user) throws IOException {
        FileNode local;
        FileNode index;
        Map<String, FileNode> localMap;
        String path;
        FolderData root;
        ImageData id;
        List<Action> actions;
        Map<String, ImageData> remoteMap;
        int errors;

        local = world.getHome().join(config.folder);
        index = local.join(".smuggler.idx");
        root = FolderData.load(index);
        remoteMap = new HashMap<>();
        root.imageMap(remoteMap);
        localMap = localMap(local);
        System.out.println("local file count: " + localMap.size());
        System.out.println("remote file count: " + localMap.size());
        actions = new ArrayList<>();
        for (FileNode file : localMap.values()) {
            id = remoteMap.get(file.getName());
            path = file.getParent().getRelative(local);
            if (id == null) {
                actions.add(new Upload(file, root, path));
            } else {
                if ((root.urlPath + "/" + path).equals(id.album.urlPath)) {
                    // no changes
                } else {
                    actions.add(new Move(id, root, path));
                }
            }
        }
        removes(localMap, remoteMap, actions);

        if (actions.isEmpty()) {
            System.out.println("nothing to do");
            return;
        }
        for (Action action : actions) {
            System.out.println(action.toString());
        }
        System.out.print("Press return to continue (" + actions.size() + " actions), ctrl-c to abort: ");
        System.in.read();
        errors = 0;
        for (Action action : actions) {
            System.out.print(action.toString());
            System.out.print(" ... ");
            try {
                action.run(user.account);
            } catch (IOException e) {
                System.out.println(" failed: " + e.getMessage());
                errors++;
            }
            System.out.println();
        }
        index.writeString(root.toString());
        System.out.println("done, errors=" + errors);
    }

    private static void removes(Map<String, FileNode> localMap, Map<String, ImageData> remoteMap, List<Action> result) {
        for (Map.Entry<String, ImageData> remote : remoteMap.entrySet()) {
            if (!localMap.containsKey(remote.getKey())) {
                result.add(new Delete(remote.getValue()));
            }
        }
    }
    private static Map<String, FileNode> localMap(FileNode root) throws IOException {
        Map<String, FileNode> result;
        FileNode old;

        result = new HashMap<>();
        for (FileNode file : root.find("**/*.JPG")) {
            old = result.put(file.getName(), file);
            if (old != null) {
                throw new IllegalStateException("duplicate file name '" + file.getName() + "': "
                        + file.getRelative(root) + " vs " + old.getRelative(root));
            }
        }
        return result;
    }
    public static abstract class Action {
        public abstract void run(Account account) throws IOException;
        public abstract String toString();
    }

    public static class Upload extends Action {
        private final FileNode file;
        private final FolderData root;
        private final String path;

        public Upload(FileNode file, FolderData root, String path) {
            this.file = file;
            this.root = root;
            this.path = path;
        }

        public void run(Account account) throws IOException {
            root.getOrCreateAlbum(account, path).upload(account, file);
        }

        public String toString() {
            return "A " + path + "/" + file.getName();
        }
    }

    public static class Move extends Action {
        private final ImageData image;
        private final FolderData root;
        private final String path;

        public Move(ImageData image, FolderData root, String path) {
            this.image = image;
            this.root = root;
            this.path = path;
        }

        @Override
        public void run(Account account) throws IOException {
            AlbumData dest;

            if (!image.album.images.remove(image)) {
                throw new IllegalStateException("not in album");
            }
            dest = root.getOrCreateAlbum(account, path);
            account.album(dest.uri).move(account.albumImage(image.uri));
            dest.images.add(new ImageData(dest, image.uri, image.fileName, image.md5));
        }

        @Override
        public String toString() {
            return "M " + image.album.urlPath + "/" + image.fileName + " ->" + path;
        }
    }

    public static class Delete extends Action {
        private final ImageData image;

        public Delete(ImageData image) {
            this.image = image;
        }

        public void run(Account account) throws IOException {
            account.albumImage(image.uri).delete();
            if (!image.album.images.remove(image)) {
                throw new IllegalStateException();
            }
        }

        public String toString() {
            return "D " + image.album.urlPath + "/" + image.fileName;
        }
    }

}
