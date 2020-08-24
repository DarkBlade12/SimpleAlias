package com.darkblade12.simplealias.plugin.reader;

import com.darkblade12.simplealias.plugin.PluginBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class Reader<T extends PluginBase, S> {
    protected final T plugin;
    protected final String resourcePath;
    protected File outputFile;

    protected Reader(T plugin, String resourcePath, File outputFile) {
        this.plugin = plugin;
        this.resourcePath = resourcePath;
        this.outputFile = outputFile;
    }

    public abstract S read();

    public abstract boolean save(S object);

    public boolean saveResourceFile() {
        InputStream input = plugin.getResource(resourcePath);
        if (input == null) {
            return false;
        }

        try {
            Files.createDirectories(outputFile.getParentFile().toPath());
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | SecurityException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean copyOutputFile(File target) {
        if (!outputFile.exists()) {
            return false;
        }

        try {
            Files.createDirectories(target.getParentFile().toPath());
            Files.copy(outputFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | SecurityException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean renameOutputFile(File dest) {
        if (!outputFile.exists()) {
            return false;
        }

        try {
            boolean success = outputFile.renameTo(dest);
            if (success) {
                this.outputFile = dest;
            }
            return success;
        } catch (SecurityException e) {
            return false;
        }
    }

    public boolean renameOutputFile(String fileName) {
        return renameOutputFile(new File(outputFile.getParentFile(), fileName));
    }

    public boolean deleteOutputFile() {
        if (!outputFile.exists()) {
            return true;
        }

        try {
            return outputFile.delete();
        } catch (SecurityException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getOutputFileName() {
        return outputFile.getName();
    }
}
