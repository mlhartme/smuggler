package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.util.List;

public class Main {
	public static void main(String[] args) throws IOException {
		World world;
		Smuggler smuggler;
		List<String> remote;
		List<FileNode> local;

		world = World.create();
		local = world.getHome().join("timeline").list();
		smuggler = Smuggler.load();
		remote = smuggler.album(smuggler.album);
		for (FileNode file : local) {
			if (!remote.contains(file.getName())) {
				System.out.print("upload " + file + " ... ");
				System.out.println(smuggler.upload(file, smuggler.album));
			}
		}
	}
}
