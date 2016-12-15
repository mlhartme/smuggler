package net.mlhartme.smuggler;

import net.mlhartme.smuggler.cache.FolderData;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CacheTest {
    @Test
    public void test() throws IOException {
        World world;
        FileNode file;
        FolderData fd;

        world = World.create();
        file = world.guessProjectHome(getClass()).join("src/test/cache");
        fd = FolderData.load(file);
        assertEquals(file.readString(), fd.toString());
    }
}
