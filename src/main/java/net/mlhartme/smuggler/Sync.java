package net.mlhartme.smuggler;

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.List;

public class Sync extends Command {
    public Sync() throws IOException {
    }

    public void run(User user) throws IOException {
        List<FileNode> local;
        Album album;
        List<AlbumImage> remote;
        int errors;

        errors = 0;
        local = world.getHome().join("timeline").list();
        album = user.lookupAlbum(config.album);
        if (album == null) {
            throw new IOException("no such album: " + album);
        }
        remote = album.listImages();
        for (FileNode file : local) {
            if (AlbumImage.lookupFileName(remote, file.getName()) == null) {
                System.out.print("A " + file);
                try {
                    album.upload(file);
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(" " + e.getMessage());
                    errors++;
                }
            }
        }
        for (AlbumImage image : remote) {
            if (lookup(local, image.fileName) == null) {
                System.out.print("D " + image.fileName);
                image.delete();
                System.out.println();
            }
        }
        if (errors > 0) {
            System.out.println("failed with errors: " + errors);
        }
    }

    private static FileNode lookup(List<FileNode> local, String fileName) {
        for (FileNode file : local) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

}
