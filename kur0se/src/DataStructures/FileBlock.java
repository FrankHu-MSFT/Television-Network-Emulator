package DataStructures;

import javafx.beans.property.SimpleStringProperty;

public class FileBlock {

	public final SimpleStringProperty fileName;;
	public final SimpleStringProperty filePath;

	public String getFileName() {
		return fileName.get();
	}

	public String getFilePath() {
		return filePath.get();
	}

	public FileBlock(String fileName, String filePath) {
		this.fileName = new SimpleStringProperty(fileName);
		this.filePath = new SimpleStringProperty(filePath);
	}
}
