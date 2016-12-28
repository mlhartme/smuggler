package net.mlhartme.smuggler;

import net.mlhartme.smuggler.cache.FolderData;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CacheTest {
    @Test
    public void root() throws IOException {
        check("cache");
    }

    @Test
    public void timeline() throws IOException {
        check("cache2");
    }

    public void check(String name) throws IOException {
        World world;
        FileNode file;
        FolderData fd;

        world = World.create();
        file = world.guessProjectHome(getClass()).join("src/test", name);
        fd = FolderData.load(file);
        assertEquals(file.readString(), fd.toString());
    }
}
