package ir.websearch.clustering.core;

import java.util.Collection;

import ir.websearch.clustering.doc.Document;
import ir.websearch.clustering.helper.StanfordLemmatizer;

public class ImprovedAlgorithm extends BasicAlgorithm {
	
	private static final double CLUSTER_CLASSIFICATION_THRESHOLD = 0.0001;	

	public ImprovedAlgorithm(Collection<Document> docs) {
		super(docs);
	}
	
	@Override
	protected double getClusterClassificationThreshold() {
		return CLUSTER_CLASSIFICATION_THRESHOLD;
	}
	
	@Override
	protected String getDocumnetText(Document doc) {
		StanfordLemmatizer slem = new StanfordLemmatizer();
		String lemmatizedText = slem.lemmatize(doc.getText());
		return lemmatizedText;
	}
	
}
