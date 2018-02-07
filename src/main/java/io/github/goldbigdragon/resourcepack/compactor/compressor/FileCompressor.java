package io.github.goldbigdragon.resourcepack.compactor.compressor;

import java.io.FilenameFilter;
import java.nio.file.Path;

/*
 * Copyright 2018 GoldBigDragon (https://github.com/GoldBigDragon) and contributors
 *
 * MinecraftResourcepackCompactor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License.

 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public interface FileCompressor extends FilenameFilter {
    /**
     * Compress the file
     * @param path the file to compress
     */
    void compress(Path path);
}
