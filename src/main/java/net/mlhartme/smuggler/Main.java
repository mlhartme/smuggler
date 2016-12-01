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

import net.mlhartme.smuggler.smugmug.Album;
import net.mlhartme.smuggler.smugmug.AlbumImage;
import net.mlhartme.smuggler.smugmug.Folder;
import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Strings;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class Main {
	public static void main(String[] args) throws IOException {
		Config config;
		World world;
		Account smugmug;

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

	public static void tree(Account smugmug, String nickName) throws IOException {
		User user;

		user = smugmug.user(nickName);
		tree(0, user.folder());
	}

	public static void tree(int indent, Folder folder) throws IOException {
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

	public static void sync(World world, Account smugmug, String userNickName, String albumName) throws IOException {
		List<FileNode> local;
		User user;
		Album album;
		List<AlbumImage> remote;
		int errors;

		errors = 0;
		local = world.getHome().join("timeline").list();
		user = smugmug.user(userNickName);
		album = user.lookupAlbum(albumName);
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

	public static class Config {
        public static Config load(World world) throws IOException {
            Properties p;

            p = world.getHome().join(".smuggler.properties").readProperties();
            return new Config(get(p, "consumer.key"), get(p, "consumer.secret"), get(p, "token.id"), get(p, "token.secret"),
                    get(p, "user"), get(p, "album"));
        }

        public static String get(Properties p, String key) {
            String value;

            value = p.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException("property not found: " + key);
            }
            return value;
        }

        public final String consumerKey;
        public final String consumerSecret;
        public final String tokenId;
        public final String tokenSecret;
        public final String user;
        public final String album;

        public Config(String consumerKey, String consumerSecret, String tokenId, String tokenSecret, String user, String album) {
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.tokenId = tokenId;
            this.tokenSecret = tokenSecret;
            this.user = user;
            this.album = album;
        }

        public Account newSmugmug() {
            return new Account(consumerKey, consumerSecret, tokenId, tokenSecret);
        }
    }
}
