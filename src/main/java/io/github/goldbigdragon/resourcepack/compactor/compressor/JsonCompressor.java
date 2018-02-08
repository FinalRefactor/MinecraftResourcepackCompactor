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

import com.google.common.io.MoreFiles;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class JsonCompressor implements FileCompressor {
    private static final String ALPHABETS = "abcdefghijklmnopqrstuvwxyz";
    private static final Map<String, Double> uselessDecimalMap;

    static {
        Map<String, Double> uselessDecimals = new HashMap<>();
        uselessDecimals.put("3333", -0.0033);
        uselessDecimals.put("6666", -0.0066);
        uselessDecimals.put("9999", -0.0099);
        uselessDecimals.put("667", +0.03);
        uselessDecimalMap = Collections.unmodifiableMap(uselessDecimals);
    }

    private final JsonParser jsonParser = new JsonParser();
    private final Gson gson = new Gson();

    @Override
    public void compress(Path path) throws IOException {
        JsonElement parsed;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            parsed = jsonParser.parse(reader);
        }
        if ("json".equals(MoreFiles.getFileExtension(path)) && parsed.isJsonObject()) {
            Map<String, String> optimizedTextureNames = new HashMap<>();
            JsonObject object = parsed.getAsJsonObject();
            JsonElement textures = object.get("textures");
            if (textures.isJsonObject()) {
                JsonObject texturesObject = textures.getAsJsonObject();
                int i = 0;
                for (String key : texturesObject.keySet()) {
                    optimizedTextureNames.put("#" + key, "#" + optimizeTextureName(i++));
                }
            }

            AtomicReference<JsonElement> result = new AtomicReference<>();
            optimize(result::set, object, true, optimizedTextureNames);
            parsed = result.get();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            gson.toJson(parsed, writer);
        }
    }

    private static boolean optimize(Consumer<JsonElement> set, JsonElement element, boolean root,
                                    Map<String, String> optimizedTextureNames) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                String str = primitive.getAsString();
                if (str.charAt(0) == '#' && optimizedTextureNames.containsKey(str)) {
                    set.accept(new JsonPrimitive(optimizedTextureNames.get(str)));
                    return true;
                }
                return false;
            }
            if (!primitive.isNumber()) {
                return false;
            }
            Number number = primitive.getAsNumber();

            String toString = number.toString();
            int dotIndex = number.toString().indexOf('.');
            if (dotIndex == -1) {
                if (number.intValue() == -0) {
                    set.accept(new JsonPrimitive(0));
                    return true;
                }
                return false;
            }
            String decimalPlaces = toString.substring(dotIndex + 1);
            if (uselessDecimalMap.containsKey(decimalPlaces)) {
                set.accept(new JsonPrimitive(number.doubleValue() + uselessDecimalMap.get(decimalPlaces)));
            }
            return true;
        } else if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                if (!optimize(array::add, jsonElement, false, optimizedTextureNames)) {
                    array.add(jsonElement);
                }
            }
            set.accept(array);
            return true;
        } else if (element.isJsonObject()) {
            JsonObject original = element.getAsJsonObject();
            JsonObject object = new JsonObject();
            for (String key : original.keySet()) {
                JsonElement jsonElement = original.get(key);
                if ("__comment".equals(key)) {
                    // keep author's copyright
                    if (root) {
                        object.add(key, jsonElement);
                    }
                    continue;
                }
                if (!optimize(it -> object.add(key, it), jsonElement, false, optimizedTextureNames)) {
                    object.add(key, jsonElement);
                }
            }
            set.accept(object);
            return true;
        } else {
            return false;
        }
    }


    private static String optimizeTextureName(int order) {
        if (order < 0) {
            return String.valueOf(order);
        }
        StringBuilder current = new StringBuilder();
        do {
            current.append(String.valueOf(ALPHABETS.charAt(order % 26)));
            order -= 26;
        } while (order < 26);
        return current.toString();
    }
}
