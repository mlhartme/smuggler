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
package net.mlhartme.smuggler.smugmug;

import java.io.IOException;

public class Handle {
    public final Account account;
    public final String uri;

    public Handle(Account account, String uri) {
        this.account = account;
        this.uri = uri;
    }

    /* also deletes the image if this is the last album it is contained in */
    public void delete() throws IOException {
        account.delete(uri);
    }

    //--

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + uri;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Handle) {
            return uri.equals(((Handle) obj).uri);
        } else {
            return false;
        }
    }
}
