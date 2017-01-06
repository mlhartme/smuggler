package net.mlhartme.smuggler.cache;

import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumData {
    public static AlbumData load(Album album, boolean full) throws IOException {
        AlbumData result;

        result = new AlbumData(album.uri, album.urlPath);
        if (full) {
            for (AlbumImage image : album.listImages()) {
                result.images.add(new ImageData(result, image.uri, image.fileName, image.md5));
            }
        }
        return result;
    }

    public static AlbumData forLine(String line) {
        int idx;

        line = Strings.removeLeft(line, "A ");
        idx = line.indexOf('@');
        if (idx == -1) {
            throw new IllegalArgumentException(line);
        }
        return new AlbumData(line.substring(idx + 1), line.substring(0, idx));
    }

    public final String uri;
    public final String urlPath;
    public final List<ImageData> images;

    public AlbumData(String uri, String urlPath) {
        this.uri = uri;
        this.urlPath = urlPath;
        this.images = new ArrayList<>();
    }

    public String name() {
        return urlPath.substring(urlPath.lastIndexOf('/') + 1);
    }

    public void imageMap(Map<String, ImageData> dest) {
        ImageData old;

        for (ImageData id : images) {
            old = dest.put(id.fileName, id);
            if (old != null) {
                System.err.println("duplicate fileName '" + id.fileName + "': " + old.album.urlPath + " vs " + id.album.urlPath);
//                throw new IllegalStateException("duplicate fileName '" + id.fileName + "': "
  //                 + old.album.urlPath + " vs " + id.album.urlPath);
            }
        }
    }

    public void upload(Account account, FileNode file) throws IOException {
        Album album;
        Account.Uploaded uploaded;

        album = account.album(uri);
        uploaded = album.upload(file);
        images.add(new ImageData(this, uploaded.albumImageUri, file.getName(), uploaded.md5));
    }

    public void toString(PrintWriter dest) {
        dest.println("A " + urlPath + "@" + uri);
        for (ImageData image : images) {
            dest.println(image.toString());
        }
    }
}
