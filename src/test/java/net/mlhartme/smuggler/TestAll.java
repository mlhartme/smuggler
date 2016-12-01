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

import com.sun.jersey.api.client.UniformInterfaceException;
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
        LOG = new PrintStream(new FileOutputStream("target/testall.log"));
        SMUGMUG.wirelog(LOG);
        user = User.forNickName(SMUGMUG, config.user);
        ROOT = user.folder(SMUGMUG);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        LOG.close();
    }

    @Before
    public void before() throws Exception {
        TEST = ROOT.lookupFolder("test");
        if (TEST != null) {
            TEST.delete();
        }
        TEST = ROOT.createFolder("test");
    }

    @Test
    public void folders() throws Exception {
        Folder sub;
        Folder subsub;
        List<Folder> lst;

        sub = TEST.createFolder("a");
        assertEquals("/Test/A", sub.urlPath);
        assertEquals(TEST, sub.parent());
        assertEquals(TEST.node(), sub.node().parent());

        assertTrue(sub.listFolders().isEmpty());
        try { // TODO
            assertTrue(sub.listAlbums().isEmpty());
            System.out.println("ok");
        } catch (UniformInterfaceException e) {
            System.out.println("failed: " + e.getMessage());
//            assertEquals(404, e.getResponse().getStatus());
        }
        assertTrue(sub.node().list().isEmpty());

        lst = TEST.listFolders();
        assertEquals(Arrays.asList(sub), lst);
        subsub = sub.createFolder("sub");
        assertEquals("/Test/A/Sub", subsub.urlPath);
        System.out.println(subsub);
        sub.delete();
    }

    @Test
    public void albums() throws Exception {
        Album album;
        String aiUri;
        AlbumImage ai;

        album = TEST.createAlbum("album");
        System.out.println("created album " + album.name);
        aiUri = album.upload(WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        System.out.println("created image " + aiUri);
        ai = SMUGMUG.albumImage(aiUri);
        ai.delete();
        System.out.println("deleted image");
        album.delete();
    }
}
