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

import net.mlhartme.smuggler.cache.FolderData;
import net.mlhartme.smuggler.smugmug.User;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.util.Diff;

import java.io.IOException;

public class Index extends Command {
    public Index() throws IOException {
    }

    public void run(User user) throws IOException {
        String str;
        String old;
        FileNode file;

        str = FolderData.load(user.folder().lookupFolder(config.album), true).toString();
        file = world.getHome().join(config.album, ".smuggler.idx");
        if (file.exists()) {
            old = file.readString();
        } else {
            old = "";
        }
        file.writeString(str);
        System.out.println(Diff.diff(old, str));
    }
}
