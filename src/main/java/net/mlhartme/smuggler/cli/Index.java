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
import net.mlhartme.smuggler.smugmug.Folder;
import net.mlhartme.smuggler.smugmug.User;

import java.io.IOException;

public class Index extends Command {
    private boolean full;

    public Index(boolean full) throws IOException {
        this.full = full;
    }

    public void run(User user) throws IOException {
        tree(user.folder());
    }

    public void tree(Folder folder) throws IOException {
        System.out.println("F " + folder.urlPath + " (@" + folder.uri + ")");
        for (Folder child : folder.listFolders()) {
            tree(child);
        }
        for (Album album : folder.listAlbums()) {
            System.out.println("A " + album.urlPath + " (@" + album.uri + ")");
            if (full) {
                for (AlbumImage image : album.listImages()) {
                    System.out.println("+ " + image.fileName + " (@" + image.uri + ") " + image.md5);
                }
            }
        }
    }
}
