package tcrn.tbi.tm.documents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.core.SentencerAnnotator;

public class TextPreprocessor implements FilePreprocessor{
	static final Logger logger = LogManager.getLogger(SentencerAnnotator.class.getName());
	File file;
	String fileString;
	@Override
	public File getTextFile() {
		return file;
	}

	@Override
	public void setTextFile(File textFile) {
		this.file = textFile;

	}

	@Override
	public String getFileString(File textFile) {
		return fileString;
	}

	@Override
	public void setFileString(String fileString) {
		this.fileString = fileString;
	}

	public String processTextFile(File textFile){
		String     ls = System.getProperty("line.separator");
		StringBuilder fileAsString = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(textFile));
			String line;
			while((line=br.readLine())!=null){
				fileAsString.append(line);
				fileAsString.append("\n");
			}
			br.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return fileAsString.toString();

	}


}
