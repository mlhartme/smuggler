package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

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
		String userName;
		String albumName;

		if (args.length != 1) {
			System.out.println("usage:");
			System.out.println("  smuggler tree");
			System.out.println("  smuggler sync");
			return;
		}
		world = World.create();
		p = world.getHome().join(".smuggler.properties").readProperties();
		userName = get(p, "user");
		albumName = get(p, "album");
		smugmug = new Smugmug(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"), get(p, "token.secret"));

		switch (args[0]) {
			case "sync":
				sync(world, smugmug, userName, albumName);
				break;
			case "tree":
				tree(smugmug, userName);
				break;
			default:
				throw new IOException("unknown command: " + args[0]);
		}
	}

	public static void tree(Smugmug smugmug, String userName) throws IOException {
		User user;

		user = new User(userName);
		tree(smugmug, 0, user.folder(smugmug));
	}

	public static void tree(Smugmug smugmug, int indent, Folder folder) throws IOException {
		Album album;

		System.out.println(Strings.times(' ', indent) + "F " + folder.urlPath + " (" + folder.nodeId + ")");
		for (Object obj : folder.list(smugmug)) {
			if (obj instanceof Folder) {
				tree(smugmug, indent + 2, (Folder) obj);
			} else {
				album = (Album) obj;
				System.out.println(Strings.times(' ', indent + 2) + "A " + album.name + " (" + album.nodeId + ")");
				/*
				for (Image image : album.list(smugmug)) {
					System.out.println(Strings.times(' ', indent + 4) + image.fileName);
				}*/
			}
		}
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
