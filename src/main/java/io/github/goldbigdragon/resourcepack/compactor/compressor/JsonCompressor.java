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

package io.github.goldbigdragon.resourcepack.compactor.compressor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonCompressor implements FileCompressor {
    private final JsonParser jsonParser = new JsonParser();
    private final Gson gson = new Gson();

    @Override
    public void compress(Path path) throws IOException {
        JsonElement parsed;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            parsed = jsonParser.parse(reader);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            gson.toJson(parsed, writer);
        }
    }
}
