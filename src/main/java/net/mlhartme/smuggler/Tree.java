package net.mlhartme.smuggler;

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.Folder;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.util.Strings;

import java.io.IOException;

public class Tree extends Command {
    public Tree() throws IOException {
    }

    public void run(User user) throws IOException {
        tree(0, user.folder());
    }

    public void tree(int indent, Folder folder) throws IOException {
        System.out.println(Strings.times(' ', indent) + "F " + folder.urlPath + " (" + folder.uri + "@" + folder.uri + ")");
        for (Folder child : folder.listFolders()) {
            tree(indent + 2, child);
        }
        for (Album album : folder.listAlbums()) {
            System.out.println(Strings.times(' ', indent + 2) + "A " + album.name + " (" + album.toString() + ")");
			/*
			for (Image image : album.list(account)) {
				System.out.println(Strings.times(' ', indent + 4) + image.fileName);
			}*/
        }
    }

}
