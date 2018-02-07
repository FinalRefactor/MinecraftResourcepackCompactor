package io.github.goldbigdragon.resourcepack.compactor.compressor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonCompressor implements FileCompressor {
    private JsonCompressor() {
    }

    private static final class SingletonHolder {
        private static final JsonCompressor INSTANCE = new JsonCompressor();
    }

    public static JsonCompressor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private final JsonParser PARSER = new JsonParser();
    private final Gson GSON = new Gson();

    @Override
    public void compress(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement parsed = PARSER.parse(reader);

            Files.write(path, GSON.toJson(parsed).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(".json");
    }
}
