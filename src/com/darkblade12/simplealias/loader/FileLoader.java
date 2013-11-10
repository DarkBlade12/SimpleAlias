package com.darkblade12.simplealias.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.plugin.Plugin;

public abstract class FileLoader {
	protected Plugin plugin;
	protected String resourceFileName;
	protected String outputPath;
	protected String outputFileName;
	protected File outputFile;

	protected FileLoader(Plugin plugin, String resourceFileName, String outputPath, String outputFileName) {
		this.plugin = plugin;
		this.resourceFileName = resourceFileName;
		this.outputPath = outputPath;
		this.outputFileName = outputFileName;
		outputFile = new File(outputPath + outputFileName);
	}

	protected FileLoader(Plugin plugin, String resourceFileName, String outputPath) {
		this.plugin = plugin;
		this.resourceFileName = resourceFileName;
		this.outputPath = outputPath;
		outputFileName = resourceFileName;
		outputFile = new File(outputPath + outputFileName);
	}

	protected boolean loadFile() {
		return !(!outputFile.exists() && !saveResourceFile());
	}

	protected void deleteFile() {
		if (outputFile.exists())
			outputFile.delete();
	}

	protected boolean saveResourceFile() {
		InputStream in = plugin.getResource(resourceFileName);
		if (in == null)
			return false;
		new File(outputPath).mkdirs();
		try {
			OutputStream out = new FileOutputStream(outputFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			out.close();
			in.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public String getResourceFileName() {
		return this.resourceFileName;
	}

	public String getOuputPath() {
		return this.outputPath;
	}

	public String getOuputFileName() {
		return this.outputFileName;
	}

	public File getOuputFile() {
		return this.outputFile;
	}
}