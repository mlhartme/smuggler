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
import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sync extends Command {
    public Sync() throws IOException {
    }

    public void run(User user) throws IOException {
        FileNode local;
        FileNode index;
        List<FileNode> files;
        AlbumData album;
        FolderData fd;
        ImageData id;
        List<Action> actions;

        local = world.getHome().join(config.folder);
        index = local.join(".smuggler.idx");
        fd = FolderData.load(index);
        files = local.find("**/*.JPG");
        System.out.println("local file count: " + files.size());
        actions = new ArrayList<>();
        for (FileNode file : files) {
            id = fd.lookupFilename(file.getName());
            if (id == null) {
                album = fd.lookupAlbum(file.getParent().getRelative(local));
                if (album == null) {
                    throw new UnsupportedOperationException("cannot create album yet");
                }
                actions.add(new Upload(file, album));
            }
        }
        if (actions.isEmpty()) {
            System.out.println("nothing to do");
            return;
        }
        for (Action action : actions) {
            System.out.println(action.toString());
        }
        System.out.print("Press return to continue, ctrl-c to abort: ");
        System.in.read();
        for (Action action : actions) {
            System.out.print(action.toString());
            System.out.print(" ... ");
            action.run(user.account);
            System.out.println();
        }
        index.writeString(fd.toString());
    }

    public static abstract class Action {
        public abstract void run(Account account) throws IOException;
        public abstract String toString();
    }

    public static class Upload extends Action {
        private final FileNode file;
        private final AlbumData albumData;

        public Upload(FileNode file, AlbumData album) {
            this.file = file;
            this.albumData = album;
        }

        public void run(Account account) throws IOException {
            Album album;
            Account.Uploaded uploaded;

            album = account.album(albumData.uri);
            uploaded = album.upload(file);
            albumData.images.add(new ImageData(albumData, uploaded.albumImageUri, file.getName(), uploaded.md5));
        }

        public String toString() {
            return "A " + file.getName();
        }
    }
}
