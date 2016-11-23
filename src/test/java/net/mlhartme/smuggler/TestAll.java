/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
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
package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class TestAll {
    @Test
    public void roundtrip() throws Exception {
        World world;
        Config config;
        Smugmug smugmug;
        User user;
        Folder root;
        Folder created;
        Album album;

        world = World.create();
        config = Config.load(world);
        smugmug = config.newSmugmug();
        try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
            smugmug.wirelog(dest);
            user = smugmug.user(config.user);
            root = user.folder(smugmug);
            System.out.println("create in " + root.uri);
            created = root.createFolder(smugmug, "folder2");
            System.out.println("created " + created.nodeId);
            created.delete(smugmug);

            album = root.createAlbum(smugmug, "album4");
            System.out.println("created " + album.name);
            album.delete(smugmug);
        }
    }
}