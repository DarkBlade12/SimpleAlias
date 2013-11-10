package com.darkblade12.simplealias.loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;

public class TextFileLoader extends FileLoader {

	public TextFileLoader(Plugin plugin, String fileName) {
		super(plugin, fileName, "plugins/" + plugin.getName() + "/");
	}

	public TextFileLoader(Plugin plugin, String directoryName, String fileName) {
		super(plugin, fileName, "plugins/" + plugin.getName() + "/" + directoryName + "/");
	}

	public boolean loadFile() {
		return super.loadFile();
	}

	public boolean saveDefaultFile() {
		return super.saveResourceFile();
	}

	public boolean saveFile(List<String> lines) {
		new File(outputPath).mkdirs();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < lines.size(); i++)
				writer.write((i > 0 ? "\n" : "") + lines.get(i));
			writer.flush();
			writer.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void deleteFile() {
		super.deleteFile();
	}

	public BufferedReader getReader() throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), "UTF-8"));
	}

	public List<String> readLines() throws Exception {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = getReader();
		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		reader.close();
		return lines;
	}
}