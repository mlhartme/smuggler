package net.mlhartme.smuggler.cache;

import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.Folder;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.io.LineReader;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FolderData implements Comparable<FolderData> {
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

    public static FolderData load(FileNode index) throws IOException {
        LineReader lr;
        String line;
        FolderData next;
        AlbumData album;
        List<FolderData> folders;
        FolderData parent;

        folders = new ArrayList<>();
        lr = index.newLineReader();
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
                    album.images.add(ImageData.forLine(album, line));
                    break;
                default:
                    throw new IOException("invalid line: " + line);
            }
        }
    }

    private static FolderData parent(List<FolderData> stack, String urlPath) {
        int idx;
        String path;
        int size;
        FolderData f;

        idx = urlPath.lastIndexOf('/');
        if (idx == -1) {
            if (!urlPath.isEmpty()) {
                throw new IllegalStateException(urlPath);
            }
            if (!stack.isEmpty()) {
                throw new IllegalStateException(stack.toString());
            }
            return null;
        }
        path = urlPath.substring(0, idx);
        size = stack.size();
        while (size > 0) {
            size--;
            f = stack.get(size);
            if (f.urlPath.equals(path)) {
                return f;
            }
            stack.remove(size);
        }
        return null;
    }

    public static FolderData forLine(String line) {
        int idx;

        line = Strings.removeLeft(line, "F ");
        idx = line.indexOf('@');
        return new FolderData(line.substring(idx + 1), line.substring(0, idx));
    }

    //--

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

    public String name() {
        return urlPath.substring(urlPath.lastIndexOf('/') + 1);
    }

    public void imageMap(Map<String, ImageData> dest) {
        for (AlbumData ad : albums) {
            ad.imageMap(dest);
        }
        for (FolderData fd : folders) {
            fd.imageMap(dest);
        }
    }

    public AlbumData getOrCreateAlbum(Account account, String path) throws IOException {
        int idx;
        String name;
        Folder folder;
        FolderData data;
        Album album;
        AlbumData nad;

        idx = path.indexOf("/");
        if (idx == -1) {
            for (AlbumData ad : albums) {
                if (path.equals(ad.name())) {
                    return ad;
                }
            }
            album = account.folder(uri).createAlbum(path);
            nad = new AlbumData(album.uri, album.urlPath);
            albums.add(nad);
            return nad;
        } else {
            name = path.substring(0, idx);
            for (FolderData fd : folders) {
                if (name.equals(fd.name())) {
                    return fd.getOrCreateAlbum(account, path.substring(idx + 1));
                }
            }
            folder = account.folder(uri).createFolder(name);
            data = new FolderData(folder.uri, folder.urlPath);
            folders.add(data);
            return data.getOrCreateAlbum(account, path.substring(idx + 1));
        }
    }

    public void sort() {
        Collections.sort(folders);
        Collections.sort(albums);
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

    @Override
    public int compareTo(FolderData o) {
        return urlPath.compareTo(o.urlPath);
    }
}
