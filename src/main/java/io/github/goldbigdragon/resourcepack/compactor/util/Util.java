package io.github.goldbigdragon.resourcepack.compactor.util;

import java.nio.file.Path;

public class Util {
    private Util() {
        throw new UnsupportedOperationException("You cannot instantiate Util");
    }

    public static String getFileExtension(String dir) {
        return dir.substring(dir.lastIndexOf('.') + 1);
    }

    public static String getFileExtension(Path path) {
        return getFileExtension(path.getFileName().toString());
    }
}
