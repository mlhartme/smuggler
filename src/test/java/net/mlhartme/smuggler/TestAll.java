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
