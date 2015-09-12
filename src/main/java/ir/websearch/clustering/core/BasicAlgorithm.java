package ir.websearch.clustering.core;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import ir.websearch.clustering.doc.Document;

public class BasicAlgorithm implements IClusterAlgorithm {
	
	private final Collection<Document> docs;
	
	public BasicAlgorithm(Collection<Document> docs) {
		this.docs = docs;
	}
	
	@Override
	public List<String> cluster() {
		List<String> output = null;
		
		try {
			// Index documents to lucene.
			Analyzer indexAnalyzer = new StandardAnalyzer();
			Directory index = new RAMDirectory();
			indexDocuments(docs, indexAnalyzer, index);
			
		} catch (Exception e) {
			System.out.println("Faild to search the collection.");
			output = null;
		}
		
		return output;
	}
	
	/**
	 * The method indexes the collection documents.
	 * @param docs the collection of documents to index. 
	 * @param indexAnalyzer the {@link Analyzer} used for indexing.
	 * @param index the index implementation of {@link Directory}.
	 * @throws IOException
	 */
	private static void indexDocuments(Collection<Document> docs, Analyzer indexAnalyzer, Directory index) throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(indexAnalyzer);
		try (IndexWriter idxWriter = new IndexWriter(index, config)) {
			for (Document doc : docs) {
				// Index document.
				addDoc(idxWriter, doc);
			}

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * The method ads a document to the index.
	 * @param writer the lucene {@link IndexWriter}
	 * @param doc the document to index.
	 * @throws IOException
	 */
	private static void addDoc(IndexWriter writer, Document doc) throws IOException {
		org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
		document.add(new StringField(Document.FILE_NUM_FIELD, doc.getFileNum(), Field.Store.YES));
		document.add(new StringField(Document.ORIG_CLUSTER_FIELD, doc.getOrigCluster(), Field.Store.YES));
		document.add(new TextField(Document.TITLE_FIELD, doc.getTitle(), Field.Store.YES));
		document.add(new TextField(Document.TEXT_FIELD, doc.getText(), Field.Store.YES));
		writer.addDocument(document);
	}

}
