package tcrn.tbi.tm.documents;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tcrn.tbi.tm.exception.SystemException;

/**
 * @author Manish
 *
 */
public class DocumentsReader {
	
	private String filePath;
	static final Logger logger = LogManager.getLogger(DocumentsReader.class.getName());

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}



	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}



	/**
	 * Return list of files if path is a folder, else returns file.
	 * @return File[]
	 * @throws SystemException Exception to throw
	 * 
	 */
	public File[] getFiles() throws SystemException{
		File [] files = null;
		logger.debug("file path is "+this.getFilePath());
		File file = new File(this.filePath);
		if(file==null || !file.exists()|| !file.canRead()){
			throw new SystemException("cannot read file from specified path");
		}
		if(file.isDirectory()){
			logger.debug("file is a directory");
			files = file.listFiles();
			logger.debug("number of files "+files.length);
		}else{
			files = new File[]{file};
		}
		return files;
	}

}
