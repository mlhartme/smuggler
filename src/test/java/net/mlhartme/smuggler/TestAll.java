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
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAll {
    private static World WORLD;
    private static Smugmug SMUGMUG;
    private static PrintStream LOG;
    private static Folder ROOT;
    private Folder TEST;

    @BeforeClass
    public static void init() throws IOException {
        User user;
        Config config;

        WORLD = World.create();
        config = Config.load(WORLD);
        SMUGMUG = config.newSmugmug();
        LOG = new PrintStream(new FileOutputStream("wire.log"));
        SMUGMUG.wirelog(LOG);
        user = SMUGMUG.user(config.user);
        ROOT = user.folder(SMUGMUG);
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
    public void folders() throws Exception {
        Folder sub;
        Folder subsub;
        List<Folder> lst;

        sub = TEST.createFolder(SMUGMUG, "a");
        assertEquals("/Test/A", sub.urlPath);
        assertTrue(sub.listFolders(SMUGMUG).isEmpty());
        //assertEquals(TEST, sub.parentFolder(SMUGMUG));
        lst = TEST.listFolders(SMUGMUG);
        assertEquals(Arrays.asList(sub), lst);
        subsub = sub.createFolder(SMUGMUG, "sub");
        assertEquals("/Test/A/Sub", subsub.urlPath);
        System.out.println(subsub);
        sub.delete(SMUGMUG);
    }

    @Test
    public void albums() throws Exception {
        Album album;
        String aiUri;
        AlbumImage ai;

        album = TEST.createAlbum(SMUGMUG, "album");
        System.out.println("created album " + album.name);
        aiUri = album.upload(SMUGMUG, WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        System.out.println("created image " + aiUri);
        ai = SMUGMUG.albumImage(aiUri);
        ai.delete(SMUGMUG);
        System.out.println("deleted image");
        album.delete(SMUGMUG);
    }
}
