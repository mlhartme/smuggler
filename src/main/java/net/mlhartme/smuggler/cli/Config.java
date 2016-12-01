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
import net.oneandone.sushi.fs.World;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by mhm on 01.12.16.
 */
public class Config {
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
