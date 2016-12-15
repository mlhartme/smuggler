package net.mlhartme.smuggler.cache;

import net.oneandone.sushi.util.Strings;

public class ImageData {
    public static ImageData forLine(String line) {
        int at;
        int hash;

        line = Strings.removeLeft(line, "+ ");
        at = line.indexOf('@');
        hash = line.indexOf('#', at);
        return new ImageData(line.substring(at + 1, hash), line.substring(0, at), line.substring(hash + 1));
    }

    public final String uri;
    public final String fileName;
    public final String md5;

    public ImageData(String uri, String fileName, String md5) {
        this.uri = uri;
        this.fileName = fileName;
        this.md5 = md5;
    }

    public String toString() {
        return "+ " + fileName + "@" + uri + "#" + md5;
    }
}
