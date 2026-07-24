package com.resideo.nextgen.reporter.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the static ResideoNextGen dashboard JSON files the React UI fetches
 * at runtime. Takes pre-built {@link JsonElement}s (see
 * {@link com.resideo.nextgen.reporter.json.JsonViewBuilder}) rather than
 * internal model objects directly -- this class is a dumb serializer, not a
 * second place that decides the output shape.
 */
public final class DashboardDataWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private DashboardDataWriter() {
    }

    public static void writeJson(Path dataDir, String fileName, JsonElement element) throws IOException {
        Path path = dataDir.resolve(fileName);
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(element, writer);
        }
    }
}
