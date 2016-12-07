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

        account = config.newSmugmug(world);
        try (PrintStream dest = new PrintStream(new FileOutputStream("wire.log"))) {
            account.wirelog(dest);
            run(account.user(config.user));
        }
    }

    public abstract void run(User user) throws IOException ;
}
