package com.darkblade12.simplealias.reader.types;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.darkblade12.simplealias.reader.Reader;

public final class SerializableReader<S extends Serializable> extends Reader<S> {
	public SerializableReader(String resourceFileName, String outputFileName, String outputPath) {
		super(resourceFileName, outputFileName, outputPath);
	}

	public SerializableReader(String fileName, String outputPath) {
		super(fileName, fileName, outputPath);
	}

	@SuppressWarnings("unchecked")
	@Override
	public S readFromFile() {
		try {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(outputFile));
			S data = (S) input.readObject();
			input.close();
			return data;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean saveToFile(S data) {
		try {
			if (!outputDirectory.isDirectory())
				outputDirectory.mkdirs();
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputFile));
			output.writeObject(data);
			output.flush();
			output.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}