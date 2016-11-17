package net.mlhartme.smuggler;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		Smuggler smuggler;

		smuggler = Smuggler.load();
		System.out.println(smuggler.album(smuggler.album).toString());

	}
}
