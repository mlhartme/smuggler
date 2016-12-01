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

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.List;

public class Sync extends Command {
    public Sync() throws IOException {
    }

    public void run(User user) throws IOException {
        List<FileNode> local;
        Album album;
        List<AlbumImage> remote;
        int errors;

        errors = 0;
        local = world.getHome().join("timeline").list();
        album = user.lookupAlbum(config.album);
        if (album == null) {
            throw new IOException("no such album: " + album);
        }
        remote = album.listImages();
        for (FileNode file : local) {
            if (AlbumImage.lookupFileName(remote, file.getName()) == null) {
                System.out.print("A " + file);
                try {
                    album.upload(file);
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(" " + e.getMessage());
                    errors++;
                }
            }
        }
        for (AlbumImage image : remote) {
            if (lookup(local, image.fileName) == null) {
                System.out.print("D " + image.fileName);
                image.delete();
                System.out.println();
            }
        }
        if (errors > 0) {
            System.out.println("failed with errors: " + errors);
        }
    }

    private static FileNode lookup(List<FileNode> local, String fileName) {
        for (FileNode file : local) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

}
