package ir.websearch.clustering.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import ir.websearch.clustering.doc.Document;

public class BasicAlgorithm implements IClusterAlgorithm {
	
	private static final String INDEX_PATH = "index";
	private final Collection<Document> docs;
	
	public BasicAlgorithm(Collection<Document> docs) {
		this.docs = docs;
	}
	
	@Override
	public List<String> cluster() {
		List<String> output = null;
		
		try {
			Path path = Files.createTempDirectory("Lucene");
			String tmpPath = path.toFile().getPath() + File.separator;
			
			// Index documents to lucene.
			Analyzer indexAnalyzer = new StandardAnalyzer(Version.LUCENE_46);
			String idxDir = tmpPath + INDEX_PATH;
			Directory index = FSDirectory.open(Paths.get(idxDir).toFile());
			indexDocuments(docs, indexAnalyzer, index);
					
			String outputVectPath = tmpPath + "luceneVect" + File.separator + "mahoutVect.vec";
			System.out.println("outputVectPath: " + outputVectPath + "." );
			
			String dicOutPath = tmpPath + "dictOut" + File.separator + "dictionary.txt";
			Path pathToFile = Paths.get(dicOutPath);
			Files.createDirectories(pathToFile.getParent());
			Files.createFile(pathToFile);
			System.out.println("dicOutPath: " + dicOutPath + "." );
			
			String norm =  "--norm " + 2;
			
			org.apache.mahout.utils.vectors.lucene.Driver.main(new String[] {
			        "--dir", idxDir,
			        "--output", outputVectPath,
			        "--field", Document.TEXT_FIELD,
			        "--idField", Document.DOC_ID_FIELD,
			        "--dictOut", dicOutPath
			    });
			
			System.out.println("Driver complete!!!");

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
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, indexAnalyzer);
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
		document.add(new StringField(Document.DOC_ID_FIELD, doc.getDocId(), Field.Store.YES));
		document.add(new StringField(Document.FILE_NUM_FIELD, doc.getFileNum(), Field.Store.YES));
		document.add(new StringField(Document.ORIG_CLUSTER_FIELD, doc.getOrigCluster(), Field.Store.YES));
		document.add(new TextField(Document.TITLE_FIELD, doc.getTitle(), Field.Store.YES));
		document.add(new TextFieldWithTermVectors(Document.TEXT_FIELD, doc.getText()));
		writer.addDocument(document);
	}
	
	static class TextFieldWithTermVectors extends Field {

	    public static final FieldType TYPE = new FieldType();

	    static {
	      TYPE.setIndexed(true);
	      TYPE.setOmitNorms(true);
	      TYPE.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS);
	      TYPE.setStored(true);
	      TYPE.setTokenized(true);
	      TYPE.setStoreTermVectors(true);
	      TYPE.freeze();
	    }

	    public TextFieldWithTermVectors(String name, String value) {
	      super(name, value, TYPE);
	    }
	  }

}
