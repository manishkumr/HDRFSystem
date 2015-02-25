package tcrn.tbi.tm.documents;

import java.io.File;

public interface FilePreprocessor {
	
	
	public File getTextFile();
	
	public void setTextFile(File textFile);
	
	public String getFileString(File textFile);
	
	public void setFileString(String fileString);
}
