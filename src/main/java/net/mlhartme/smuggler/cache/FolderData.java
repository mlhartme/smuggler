package net.mlhartme.smuggler.cache;

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.Folder;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.io.LineReader;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class FolderData {
    public static FolderData load(Folder folder, boolean full) throws IOException {
        FolderData result;

        result = new FolderData(folder.uri, folder.urlPath);
        for (Folder child : folder.listFolders()) {
            result.folders.add(load(child, full));
        }
        for (Album album : folder.listAlbums()) {
            result.albums.add(AlbumData.load(album, full));
        }
        return result;
    }

    public static FolderData load(FileNode node) throws IOException {
        LineReader lr;
        String line;
        FolderData next;
        AlbumData album;
        List<FolderData> folders;
        FolderData parent;

        folders = new ArrayList<>();
        lr = node.newLineReader();
        album = null;
        while (true) {
            line = lr.next();
            if (line == null) {
                return folders.get(0);
            }
            switch (line.charAt(0)) {
                case 'F':
                    next = FolderData.forLine(line);
                    parent = parent(folders, next.urlPath);
                    if (parent != null) {
                        parent.folders.add(next);
                    } else {
                        // root folder
                    }
                    folders.add(next);
                    break;
                case 'A':
                    album = AlbumData.forLine(line);
                    parent(folders, album.urlPath).albums.add(album);
                    break;
                case '+':
                    album.images.add(ImageData.forLine(line));
                    break;
                default:
                    throw new IOException("invalid line: " + line);
            }
        }
    }

    private static FolderData parent(List<FolderData> folders, String urlPath) {
        int idx;
        String path;
        int size;
        FolderData f;

        idx = urlPath.lastIndexOf('/');
        if (idx == -1) {
            if (!folders.isEmpty()) {
                throw new IllegalStateException();
            }
            return null;
        }
        path = urlPath.substring(0, idx);
        size = folders.size();
        while (size > 0) {
            size--;
            f = folders.get(size);
            if (f.urlPath.equals(path)) {
                return f;
            }
            folders.remove(size);
        }
        throw new IllegalStateException();
    }

    public static FolderData forLine(String line) {
        int idx;

        line = Strings.removeLeft(line, "F ");
        idx = line.indexOf('@');
        return new FolderData(line.substring(idx + 1), line.substring(0, idx));
    }

    public final String uri;
    public final String urlPath;
    public final List<FolderData> folders;
    public final List<AlbumData> albums;

    public FolderData(String uri, String urlPath) {
        this.uri = uri;
        this.urlPath = urlPath;
        this.folders = new ArrayList<>();
        this.albums = new ArrayList<>();
    }

    public void toString(PrintWriter dest) {
        dest.println("F " + urlPath + "@" + uri);
        for (FolderData child : folders) {
            child.toString(dest);
        }
        for (AlbumData child : albums) {
            child.toString(dest);
        }
    }

    public String toString() {
        StringWriter dest = new StringWriter();
        PrintWriter pw = new PrintWriter(dest);
        toString(pw);
        return dest.toString();
    }
}
