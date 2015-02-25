package tcrn.tbi.tm.util;

import java.io.File;

public class I2B2Utils {
	
	/**
	 * @param fileName FileName to find
	 * @param goldAnnFiles File forlder to search 
	 * @return File if found else null 
	 */
	public static File findGoldFile(String fileName, File[] goldAnnFiles) {
		File goldFile = null;
		for (File file : goldAnnFiles) {
			if(file.getName().split("\\.")[0].equals(fileName.split("\\.")[0])){
				return file;
			}
		}
		return goldFile;
	}

}
