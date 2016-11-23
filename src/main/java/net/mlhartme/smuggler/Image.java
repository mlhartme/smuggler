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

import java.util.List;

public class Image {
    public static Image lookupFileName(List<Image> images, String fileName) {
        for (Image image : images) {
            if (image.fileName.equals(fileName)) {
                return image;
            }
        }
        return null;
    }

    //--

    public final String key;
    public final String fileName;

    public Image(String key, String fileName) {
        this.key = key;
        this.fileName = fileName;
    }

    public void delete(Smugmug smugmug) {
        smugmug.api("/api/v2/image/" + key).delete();
    }
}
