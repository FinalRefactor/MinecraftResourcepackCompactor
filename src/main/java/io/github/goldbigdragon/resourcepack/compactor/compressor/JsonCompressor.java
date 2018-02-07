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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class JsonCompressor implements FileCompressor {
    private final JsonParser jsonParser = new JsonParser();
    private final Gson gson = new Gson();

    @Override
    public void compress(Path path) throws IOException {
        JsonElement parsed;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            parsed = jsonParser.parse(reader);
        }
        if ("json".equals(MoreFiles.getFileExtension(path)) && parsed.isJsonObject()) {
            // modeling file...
            // todo: texture optimization
            AtomicReference<JsonElement> result = new AtomicReference<>();
            optimize(result::set, parsed, true);
            parsed = result.get();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            gson.toJson(parsed, writer);
        }
    }

    private static boolean optimize(Consumer<JsonElement> set, JsonElement element, boolean root) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isNumber()) {
                return false;
            }
            Number number = primitive.getAsNumber();
            if (number.toString().indexOf('.') == -1) {
                if (number.intValue() == -0) {
                    set.accept(new JsonPrimitive(0));
                    return true;
                }
                return false;
            }
            double doubleValue = number.doubleValue();
            long longValue = number.longValue();
            if (doubleValue == longValue) {
                return false;
            }
            set.accept(new JsonPrimitive(
                    new BigDecimal(String.valueOf(doubleValue)).setScale(2, RoundingMode.CEILING).floatValue()));
            return true;
        } else if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                if (!optimize(array::add, jsonElement, false)) {
                    array.add(jsonElement);
                } else {
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
                if (!optimize(it -> object.add(key, it), jsonElement, false)) {
                    object.add(key, jsonElement);
                }
            }
            set.accept(object);
            return true;
        } else {
            return false;
        }
    }

    private String getConvertedTextureName(int order) {
        String amount = Integer.toString(order / 26);
        order %= 26;
        return Character.toString((char) (order + 97)) + (amount.equals("0") ? "" : amount);
    }
}
