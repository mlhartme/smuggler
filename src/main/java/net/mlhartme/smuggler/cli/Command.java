package net.mlhartme.smuggler.cli;

import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.World;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Command {
    protected final World world;
    protected final Config config;

    public Command() throws IOException {
        world = World.create();
        config = Config.load(world);
    }

    public void run() throws IOException {
        Account account;

        account = config.newSmugmug();
        try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
            account.wirelog(dest);
            run(account.user(config.user));
        }
    }

    public abstract void run(User user) throws IOException ;
}
