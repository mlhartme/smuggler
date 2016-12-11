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
package net.mlhartme.smuggler;

import net.mlhartme.smuggler.cli.Config;
import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.mlhartme.smuggler.smugmug.Folder;
import net.mlhartme.smuggler.smugmug.Image;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TestAll {
    private static World WORLD;
    private static Account SMUGMUG;
    private static PrintStream LOG;
    private static Folder ROOT;
    private Folder TEST;

    @BeforeClass
    public static void init() throws IOException {
        User user;
        Config config;

        WORLD = World.create();
        config = Config.load(WORLD);
        SMUGMUG = config.newSmugmug(WORLD);
        LOG = new PrintStream(new FileOutputStream("target/testall.log"));
        SMUGMUG.wirelog(LOG);
        user = SMUGMUG.user(config.user);
        ROOT = user.folder();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        LOG.close();
    }

    @Before
    public void before() throws Exception {
        TEST = ROOT.lookupFolder("test2");
        if (TEST != null) {
            TEST.delete();
        }
        TEST = ROOT.createFolder("test2");
    }

    @Test
    public void folders() throws Exception {
        Folder sub;
        Folder subsub;
        List<Folder> lst;

        sub = TEST.createFolder("a");
        assertEquals("/Test2/A", sub.urlPath);
        assertEquals(TEST, sub.parent());
        assertEquals(TEST.node(), sub.node().parent());

        assertTrue(sub.listFolders().isEmpty());
        assertTrue(sub.listAlbums().isEmpty());
        assertTrue(sub.node().list().isEmpty());

        lst = TEST.listFolders();
        assertEquals(Arrays.asList(sub), lst);
        subsub = sub.createFolder("sub");
        assertEquals("/Test2/A/Sub", subsub.urlPath);
        System.out.println(subsub);
        sub.delete();
    }

    @Test
    public void albums() throws Exception {
        Album album;
        String aiUri;
        AlbumImage ai;

        album = TEST.createAlbum("album");
        assertEquals(TEST, album.folder());
        assertEquals(TEST.node(), album.folder().node());
        assertEquals(Collections.singletonList(album), TEST.listAlbums());
        assertEquals(Collections.singletonList(album.node()), TEST.node().list());
        assertTrue(album.listImages().isEmpty());
        aiUri = album.upload(WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        ai = SMUGMUG.albumImage(aiUri);
        assertEquals(Collections.singletonList(ai), album.listImages());
        ai.delete();
        album.delete();
    }

    @Test
    public void images() throws Exception {
        String md5;
        Node<?> file;
        Album album;
        String aiUri;
        AlbumImage ai;
        AlbumImage copy;
        Image image;
        String ai2;

        file = WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg");
        md5 = file.md5();
        album = TEST.createAlbum("album");
        aiUri = album.upload(file);
        ai = SMUGMUG.albumImage(aiUri);
        assertEquals("mhm.jpg", ai.fileName);
        assertEquals(md5, ai.md5);
        assertEquals(album, ai.album());

        image = ai.image();
        assertEquals("mhm.jpg", image.fileName);
        assertEquals(md5, image.md5);

        // uploading the same image twice creates two copies
        ai2 = album.upload(file, "copy.jpg");
        copy = SMUGMUG.albumImage(ai2);
        assertNotEquals(ai, copy);
        assertNotEquals(ai.image(), copy.image());
        assertEquals(ai.md5, copy.md5);

        copy.image().setTitle("some title");
        album.delete();
    }

    @Test
    public void moveImageAlbum() throws Exception {
        Album first;
        Album second;
        String aiUri;
        AlbumImage ai;
        List<AlbumImage> lst;
        Image image;
        String imageUri;

        first = TEST.createAlbum("first");
        aiUri = first.upload(WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        ai = SMUGMUG.albumImage(aiUri);
        image = ai.image();
        imageUri = image.uri;
        assertEquals(first, image.album());

        second = TEST.createAlbum("second");
        second.move(ai);

        assertTrue(first.listImages().isEmpty());
        lst = second.listImages();
        assertEquals(1, lst.size());
        ai = lst.get(0);
        assertEquals(second, ai.album());

        image = ai.image();
        assertEquals(imageUri, image.uri);
        assertEquals(second, image.album());
    }

    @Test
    public void collectImageAlbum() throws Exception {
        Album first;
        Album second;
        String aiUri;
        AlbumImage ai;
        List<AlbumImage> lst;
        Image image;
        String imageUri;

        first = TEST.createAlbum("first");
        aiUri = first.upload(WORLD.guessProjectHome(getClass()).join("src/test/mhm.jpg"));
        ai = SMUGMUG.albumImage(aiUri);
        image = ai.image();
        imageUri = image.uri;
        assertEquals(first, image.album());

        second = TEST.createAlbum("second");
        second.collect(ai);

        assertEquals(Collections.singletonList(ai), first.listImages());

        lst = second.listImages();
        assertEquals(1, lst.size());
        ai = lst.get(0);
        assertEquals(second, ai.album());

        image = ai.image();
        assertEquals(imageUri, image.uri);
        assertEquals(first, image.album());

        image.delete();
        assertTrue(first.listImages().isEmpty());
        assertTrue(second.listImages().isEmpty());
    }
}
