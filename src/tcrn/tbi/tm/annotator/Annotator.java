package tcrn.tbi.tm.annotator;

import tcrn.tbi.tm.model.FileAnnotation;
import tcrn.tbi.tm.model.Annotation;

public interface Annotator {
	
	
	public Annotation annotate();
	public FileAnnotation annotate(FileAnnotation annFile);

}
