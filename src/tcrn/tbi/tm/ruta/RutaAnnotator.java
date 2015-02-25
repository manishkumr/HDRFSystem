package tcrn.tbi.tm.ruta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

import tcrn.tbi.tm.model.Annotation;
import tcrn.tbi.tm.model.FileAnnotation;

public class RutaAnnotator {

	public  List<Annotation> annotate(FileAnnotation annFile,String rutaType) throws  Exception {
		AnalysisEngine engine = AnalysisEngineFactory.createEngine("abbreviationRulesEngine");
		CAS cas = engine.newCAS();
		cas.setDocumentText(annFile.getCoveredText());
		engine.process(cas);
		TypeSystem ts = cas.getTypeSystem();  
		Iterator<Type> typeItr = ts.getTypeIterator();
		List<Annotation> annList = new ArrayList<Annotation>();
		while (typeItr.hasNext()) {
			Type type = (Type) typeItr.next();
			if (type.getName().equals(rutaType)) {
				AnnotationIndex<AnnotationFS> annotations2 = cas.getAnnotationIndex(type);
				for (AnnotationFS afs : annotations2)
				{
					String annText   = afs.getCoveredText();
					annText = annText.replaceAll("\\n", " ");
					Integer startOffset = afs.getBegin();
					Integer endOffset = afs.getEnd();
					Annotation ann = new Annotation();
					ann.setCoveredText(annText);
					ann.setStartOffset(startOffset);
					ann.setEndOffset(endOffset);
					annList.add(ann);
				}
			}
		}
		return annList;
	}
}
