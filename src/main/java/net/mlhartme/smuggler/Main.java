package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class Main {
	public static void main(String[] args) throws IOException {
		World world;
		Smuggler smuggler;
		List<String> remote;
		List<FileNode> local;
		Map<String, String> albums;
		String albumKey;

		world = World.create();
		local = world.getHome().join("timeline").list();
		smuggler = Smuggler.load();
		try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
			smuggler.wirelog(dest);
			albums = smuggler.listAlbums(smuggler.user);
			albumKey = albums.get(smuggler.album);
			if (albumKey == null) {
				throw new IOException("no such album: " + smuggler.album);
			}
			remote = smuggler.album(albumKey);
			for (FileNode file : local) {
				if (!remote.contains(file.getName())) {
					System.out.print("upload " + file + " ... ");
					System.out.println(smuggler.upload(file, smuggler.album));
				}
			}
		}
	}
}
