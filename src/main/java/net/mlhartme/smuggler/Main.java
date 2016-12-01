/*
 * Copyright Michael Hartmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mlhartme.smuggler;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.*;
import java.util.List;

public class Main {
	public static void main(String[] args) throws IOException {
		Config config;
		World world;
		Smugmug smugmug;

		if (args.length != 1) {
			System.out.println("usage:");
			System.out.println("  smuggler tree");
			System.out.println("  smuggler sync");
			return;
		}
		world = World.create();
		config = Config.load(world);
		smugmug = config.newSmugmug();
		try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
			smugmug.wirelog(dest);
			switch (args[0]) {
				case "sync":
					sync(world, smugmug, config.user, config.album);
					break;
				case "tree":
					tree(smugmug, config.user);
					break;
				default:
					throw new IOException("unknown command: " + args[0]);
			}
		}
	}

	public static void tree(Smugmug smugmug, String userName) throws IOException {
		User user;

		user = User.forNickName(smugmug, userName);
		tree(smugmug, 0, user.folder(smugmug));
	}

	public static void tree(Smugmug smugmug, int indent, Folder folder) throws IOException {
		System.out.println(Strings.times(' ', indent) + "F " + folder.urlPath + " (" + folder.uri + "@" + folder.uri + ")");
		for (Folder child : folder.listFolders()) {
			tree(smugmug, indent + 2, child);
		}
		for (Album album : folder.listAlbums()) {
			System.out.println(Strings.times(' ', indent + 2) + "A " + album.name + " (" + album.toString() + ")");
			/*
			for (Image image : album.list(smugmug)) {
				System.out.println(Strings.times(' ', indent + 4) + image.fileName);
			}*/
		}
	}

	public static void sync(World world, Smugmug smugmug, String userNickName, String albumName) throws IOException {
		List<FileNode> local;
		User user;
		Album album;
		List<AlbumImage> remote;
		int errors;

		errors = 0;
		local = world.getHome().join("timeline").list();
		user = User.forNickName(smugmug, userNickName);
		album = user.lookupAlbum(smugmug, albumName);
		if (album == null) {
			throw new IOException("no such album: " + albumName);
		}
		remote = album.listImages();
		for (FileNode file : local) {
			if (AlbumImage.lookupFileName(remote, file.getName()) == null) {
				System.out.print("A " + file);
				try {
					album.upload(file);
					System.out.println();
				} catch (Exception e) {
					System.out.println(" " + e.getMessage());
					errors++;
				}
			}
		}
		for (AlbumImage image : remote) {
			if (lookup(local, image.fileName) == null) {
				System.out.print("D " + image.fileName);
				image.delete();
				System.out.println();
			}
		}
		if (errors > 0) {
			System.out.println("failed with errors: " + errors);
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
