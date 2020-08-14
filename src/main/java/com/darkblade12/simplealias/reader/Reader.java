package com.darkblade12.simplealias.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.google.common.io.Files;

public abstract class Reader<D> {
	protected String resourceFileName;
	protected String outputFileName;
	protected String outputPath;
	protected File outputDirectory;
	protected File outputFile;

	public Reader(String resourceFileName, String outputFileName, String outputPath) {
		this.resourceFileName = resourceFileName;
		this.outputFileName = outputFileName;
		if (!outputPath.endsWith("/"))
			outputPath += "/";
		this.outputPath = outputPath;
		outputDirectory = new File(outputPath);
		outputFile = new File(outputPath + outputFileName);
		if (outputFile.isDirectory())
			throw new IllegalArgumentException("Output file cannot be a directory");
	}

	public abstract D readFromFile();

	public boolean saveResourceFile() {
		try {
		    InputStream inputStream = SimpleAlias.instance().getResource(resourceFileName);
		    byte[] buffer = new byte[inputStream.available()];
		    inputStream.read(buffer);
			Files.write(buffer, outputFile);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public abstract boolean saveToFile(D data);

	public boolean copyFile(File destination) {
		if (isOutputFileReadable())
			try {
				Files.copy(outputFile, destination);
				return true;
			} catch (IOException e) {
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
				return false;
			}
		else
			return false;
	}

	public boolean deleteFile() {
		return outputFile.delete();
	}

	public void setResourceFileName(String resourceFileName) {
		this.resourceFileName = resourceFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
		setOutputFile(new File(outputPath + outputFileName));
	}

	public void setOutputPath(String outputPath) {
		if (!outputPath.endsWith("/"))
			outputPath += "/";
		File outputDirectory = new File(outputPath);
		if (outputDirectory.exists() && !outputDirectory.isDirectory())
			throw new IllegalArgumentException("Output directory cannot be a normal file");
		this.outputPath = outputPath;
		this.outputDirectory = outputDirectory;
		setOutputFile(new File(outputPath + outputFileName));
	}

	public void setOutputDirectory(File outputDirectory) {
		setOutputPath(outputDirectory.getPath());
	}

	public void setOutputFile(File outputFile) {
		if (outputFile.isDirectory())
			throw new IllegalArgumentException("Output file cannot be a directory");
		if (this.outputFile.exists())
			this.outputFile.renameTo(outputFile);
		this.outputFile = outputFile;
	}

	public String getResourceFileName() {
		return this.resourceFileName;
	}

	public String getOuputFileName() {
		return this.outputFileName;
	}

	public String getOuputPath() {
		return this.outputPath;
	}

	public File getOutputDirectory() {
		return this.outputDirectory;
	}

	public File getOuputFile() {
		return this.outputFile;
	}

	public boolean isOutputFileReadable() {
		return outputFile.exists();
	}
}