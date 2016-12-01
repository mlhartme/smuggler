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
import net.oneandone.sushi.util.Strings;

import java.io.IOException;

public class Index extends Command {
    public Index() throws IOException {
    }

    public void run(User user) throws IOException {
        tree(0, user.folder());
    }

    public void tree(int indent, Folder folder) throws IOException {
        System.out.println(Strings.times(' ', indent) + "F " + folder.urlPath + " (" + folder.uri + "@" + folder.uri + ")");
        for (Folder child : folder.listFolders()) {
            tree(indent + 2, child);
        }
        for (Album album : folder.listAlbums()) {
            System.out.println(Strings.times(' ', indent + 2) + "A " + album.name + " (" + album.toString() + ")");
			for (AlbumImage image : album.listImages()) {
				System.out.println(Strings.times(' ', indent + 4) + image.fileName);
			}
        }
    }

}
