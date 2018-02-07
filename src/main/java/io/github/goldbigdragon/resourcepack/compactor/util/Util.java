/*
 * This file is part of MinecraftResourceCompactor.
 *
 * MinecraftResourcepackCompactor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * MinecraftResourcepackCompactor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
