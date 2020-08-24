package com.darkblade12.simplealias.plugin.reader;

import com.darkblade12.simplealias.plugin.PluginBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class JsonReader<T extends PluginBase, S> extends Reader<T, S> {
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Class<S> type;

    public JsonReader(T plugin, Class<S> type, String resourcePath, File outputFile) {
        super(plugin, resourcePath, outputFile);
        this.type = type;
    }

    public JsonReader(T plugin, Class<S> type, File outputFile) {
        this(plugin, type, outputFile.getName(), outputFile);
    }

    public static boolean isJson(String fileName) {
        return FILE_EXTENSION_PATTERN.matcher(fileName).find();
    }

    public static boolean isJson(File file) {
        return isJson(file.getName());
    }

    public static String stripExtension(String fileName) {
        return FILE_EXTENSION_PATTERN.matcher(fileName).replaceFirst("");
    }

    public static String stripExtension(File file) {
        return stripExtension(file.getName());
    }

    @Override
    public S read() {
        if (!outputFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), StandardCharsets.UTF_8))) {
            return GSON.fromJson(reader, type);
        } catch (IOException | JsonSyntaxException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean save(S object) {
        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                GSON.toJson(object, writer);
            }
            return true;
        } catch (IOException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
