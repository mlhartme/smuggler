package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.DirectoryNotFoundException;
import net.oneandone.sushi.fs.ListException;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class Main {
	private static String get(Properties p, String key) {
		String value;

		value = p.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("property not found: " + key);
		}
		return value;
	}

	public static void main(String[] args) throws IOException {
		Properties p;
		World world;
		Smugmug smugmug;
		User user;
		Album album;
		String userName;
		String albumName;
		List<Image> remote;
		List<FileNode> local;

		world = World.create();
		p = world.getHome().join(".smuggler.properties").readProperties();
		userName = get(p, "user");
		albumName = get(p, "album");
		smugmug = new Smugmug(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"), get(p, "token.secret"));

		// sync(smugmug, world, userName, albumName);
		tree(smugmug, userName);
	}

	public static void tree(Smugmug smugmug, String userName) throws IOException {
		User user;
		Folder folder;

		user = new User(userName);
		folder = user.folder(smugmug);
		System.out.println(folder.nodeId + " " + folder.urlPath);
	}

	public static void sync(World world, Smugmug smugmug, String userName, String albumName) throws IOException {
		List<FileNode> local;
		User user;
		Album album;
		List<Image> remote;

		local = world.getHome().join("timeline").list();
		try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
			smugmug.wirelog(dest);
			user = new User(userName);
			album = user.lookupAlbum(smugmug, albumName);
			if (album == null) {
				throw new IOException("no such album: " + albumName);
			}
			remote = album.list(smugmug);
			for (FileNode file : local) {
				if (Image.lookupFileName(remote, file.getName()) == null) {
					System.out.print("A " + file);
					System.out.println(album.upload(smugmug, file));
				}
			}
			for (Image image : remote) {
				if (lookup(local, image.fileName) == null) {
					System.out.print("D " + image.fileName);
					image.delete(smugmug);
				}
			}
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
