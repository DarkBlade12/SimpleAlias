package com.darkblade12.simplealias.plugin.reader;

import com.darkblade12.simplealias.plugin.PluginBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;

public class SerializableReader<T extends PluginBase, S extends Serializable> extends Reader<T, S> {
    public SerializableReader(T plugin, String resourcePath, File outputFile) {
        super(plugin, resourcePath, outputFile);
    }

    public SerializableReader(T plugin, File outputFile) {
        super(plugin, outputFile.getName(), outputFile);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S read() {
        if (!outputFile.exists()) {
            return null;
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(outputFile))) {
            return (S) input.readObject();
        } catch (Exception e) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            output.writeObject(object);
            output.flush();
            return true;
        } catch (IOException | SecurityException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
