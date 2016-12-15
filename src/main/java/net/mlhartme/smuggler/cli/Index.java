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

import java.io.IOException;

public class Index extends Command {
    private boolean full;

    public Index(boolean full) throws IOException {
        this.full = full;
    }

    public void run(User user) throws IOException {
        System.out.println(FolderData.load(user.folder(), full));
    }
}
