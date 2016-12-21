package net.mlhartme.smuggler.cache;

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AlbumData {
    public static AlbumData load(Album album, boolean full) throws IOException {
        AlbumData result;

        result = new AlbumData(album.uri, album.urlPath);
        if (full) {
            for (AlbumImage image : album.listImages()) {
                result.images.add(new ImageData(image.uri, image.fileName, image.md5));
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

    public ImageData lookupFilename(String filename) {
        ImageData result;

        for (ImageData id : images) {
            if (filename.equals(id.fileName)) {
                return id;
            }
        }
        return null;
    }

    public void toString(PrintWriter dest) {
        dest.println("A " + urlPath + "@" + uri);
        for (ImageData image : images) {
            dest.println(image.toString());
        }
    }
}
