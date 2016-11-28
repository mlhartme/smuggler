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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TestAll {
    private static World WORLD;
    private static Config CONFIG;
    private static Smugmug SMUGMUG;
    private static PrintStream LOG;
    private static User USER;
    private static Folder ROOT;
    private Folder TEST;

    @BeforeClass
    public static void init() throws IOException {
        WORLD = World.create();
        CONFIG = Config.load(WORLD);
        SMUGMUG = CONFIG.newSmugmug();
        LOG = new PrintStream(new FileOutputStream("wire.log"));
        SMUGMUG.wirelog(LOG);
        USER = SMUGMUG.user(CONFIG.user);
        ROOT = USER.folder(SMUGMUG);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        LOG.close();
    }

    @Before
    public void before() throws Exception {
        TEST = ROOT.lookupFolder(SMUGMUG, "test");
        if (TEST != null) {
            TEST.delete(SMUGMUG);
        }
        TEST = ROOT.createFolder(SMUGMUG, "test");
    }

    @Test
    public void roundtrip() throws Exception {
        Folder created;
        Album album;
        String aiUri;
        AlbumImage ai;

        created = TEST.createFolder(SMUGMUG, "folder");
        System.out.println("created folder " + created.nodeId);
        created.delete(SMUGMUG);

        album = TEST.createAlbum(SMUGMUG, "test-album");
        System.out.println("created album " + album.name);
        aiUri = album.upload(SMUGMUG, WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        System.out.println("created image " + aiUri);
        ai = SMUGMUG.albumImage(aiUri);
        ai.delete(SMUGMUG);
        System.out.println("deleted image");
        album.delete(SMUGMUG);
    }
}
