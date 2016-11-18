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
        smugmug.resource("https://api.smugmug.com/api/v2/image/" + key).delete();
    }
}
