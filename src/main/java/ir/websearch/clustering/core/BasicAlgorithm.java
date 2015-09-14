package ir.websearch.clustering.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.clustering.kmeans.RandomSeedGenerator;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.utils.clustering.ClusterDumper;

import com.sun.tools.javac.util.Pair;

import ir.websearch.clustering.doc.Document;

public class BasicAlgorithm implements IClusterAlgorithm {
	
	private static final int MAX_ITERATIONS = 10;
	private static final String INDEX_PATH = "index";
	private static final int K = 5;
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
			Path dicOutFilePath = Paths.get(dicOutPath);
			Files.createDirectories(dicOutFilePath.getParent());
			System.out.println("dicOutPath: " + dicOutPath + "." );
			
			String norm =  "--norm " + 2;
			
			// Convert lucene term vectors to Mahout vectors.
			org.apache.mahout.utils.vectors.lucene.Driver.main(new String[] {
			        "--dir", idxDir,
			        "--output", outputVectPath,
			        "--field", Document.TEXT_FIELD,
			        "--idField", Document.DOC_ID_FIELD,
			        "--dictOut", dicOutPath
			    });
			
			System.out.println("Driver write Mahout vectors complete!!!");
			
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.getLocal(conf);
			org.apache.hadoop.fs.Path vectorPath = new org.apache.hadoop.fs.Path(outputVectPath);
			org.apache.hadoop.fs.Path initCentroidsPath = new org.apache.hadoop.fs.Path(tmpPath, "InitCentroids");			
		    
			// Select initial centroids
			DistanceMeasure measure = new EuclideanDistanceMeasure();
		    RandomSeedGenerator.buildRandom(conf, vectorPath, initCentroidsPath, K, measure);

		    // Run k-means
		    org.apache.hadoop.fs.Path kMeansOutput = new org.apache.hadoop.fs.Path(tmpPath, "kmeansOutput");
		    KMeansDriver.run(conf, vectorPath, initCentroidsPath, kMeansOutput, 0.001, MAX_ITERATIONS, true, 0.0, true);

		    // Print out clusters
		    String[] termDictionary = null;
			try {
				List<String> terms = new ArrayList<>();
				List<String> dicLins = Files.readAllLines(dicOutFilePath);
				int termCount = Integer.parseInt(dicLins.get(0));
				for (int i = 2; i < 2 + termCount; i++) {
					String line = dicLins.get(i);
					String[] columns = line.split("\t");
					String term = columns[0];
					terms.add(term);
				}
				
				termDictionary = terms.toArray(new String[terms.size()]);
			} catch (IOException e) {
				// TODO handle exception.
			}
		    
		    org.apache.hadoop.fs.Path finalClusterPath = finalClusterPath(conf, kMeansOutput, MAX_ITERATIONS);
		    String finalClusterDir = finalClusterPath.toString();
		    ClusterDumper clusterDumper = new ClusterDumper();
		    String clusterResultsFile = tmpPath + "kmeansOutput" + File.separator + "clusterResults.txt";
			clusterDumper.run(new String[] {
			        "--input", finalClusterDir,
			        "--dictionary", dicOutPath,
			        "--dictionaryType", "text",
			        "--output", clusterResultsFile,
			        "--outputFormat", "CSV",
			        "--pointsDir", tmpPath + "kmeansOutput" + File.separator + "clusteredPoints",
			        "--distanceMeasure", EuclideanDistanceMeasure.class.getName()
			    });
		    
		    output = generateOutputLines(clusterResultsFile);
		} catch (Exception e) {
			System.out.println("Faild to search the collection.");
			output = null;
		}
		
		return output;
	}

	private List<String> generateOutputLines(String clusterResultsFile) throws IOException {
		List<String> outputLines = new ArrayList<>();
		int clusterID = 0;			
		Path clusterResultsFilePath = Paths.get(clusterResultsFile);
		List<String> clusteringResults = Files.readAllLines(clusterResultsFilePath);
		if (CollectionUtils.isNotEmpty(clusteringResults)) {
			Map<String, Integer> docIdToClusterID = new HashMap<>(); 
			for (String clusterDocs : clusteringResults) {
				clusterID++;
				String[] columns = clusterDocs.split(",");
				List<String> docIDs = Arrays.asList(Arrays.copyOfRange(columns, 1, columns.length));
				final Integer currClusterID = clusterID;
				Map<String, Integer> docIDClusterID = docIDs.stream()
						.map(docID -> new ImmutablePair<String, Integer>(docID, currClusterID))
						.collect(Collectors.toMap(pair -> pair.getKey(), pair -> pair.getValue()));
				
				docIdToClusterID.putAll(docIDClusterID);
			}
					    	
			docIdToClusterID.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> {
					String line = entry.getKey() + "," + entry.getValue();
					outputLines.add(line);
				});		    	
		}
		return outputLines;
	}
	
  /**
   * Return the path to the final iteration's clusters
   */
	private static org.apache.hadoop.fs.Path finalClusterPath(Configuration conf, org.apache.hadoop.fs.Path output,
			int maxIterations) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		for (int i = maxIterations; i >= 0; i--) {
			org.apache.hadoop.fs.Path clusters = new org.apache.hadoop.fs.Path(output, "clusters-" + i + "-final");
			if (fs.exists(clusters)) {
				return clusters;
			}
		}
		
		return null;
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
