package ir.websearch.clustering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import ir.websearch.clustering.core.BasicAlgorithm;
import ir.websearch.clustering.core.IClusterAlgorithm;
import ir.websearch.clustering.core.ImprovedAlgorithm;
import ir.websearch.clustering.doc.Document;
import ir.websearch.clustering.doc.DocumentsParser;
import ir.websearch.clustering.helper.InputParams;
import ir.websearch.clustering.helper.InputParams.Parser;

public class Cluster {

	private static final String BASIC_ALGORITHM = "basic";
	private static final String IMPROVED_ALGORITHM = "improved";

	public static void main(String[] args) {
		if (args.length != 1) {
			// must accept only one parameter which is the name of a parameter file.
			System.out.println("Must include the parameter file name");
			return;
		}

		String fileName = args[0];
		Parser inputParser = new Parser(fileName);
		InputParams inputParams = inputParser.parse();
		if (inputParams == null) {
			System.out.println("Faild to load parameter file name: " + fileName + ".");
			return;
		}
		
		DocumentsParser docsParser = new DocumentsParser(inputParams.getRootDirectory());
		Collection<Document> docs = docsParser.parse();
		if (docs == null) {
			System.out.println("Faild to load documents from root directory: " + inputParams.getRootDirectory() + ".");
			return;
		}
		
		// Generate the a cluster algorithm of choice and perform clustering.
		IClusterAlgorithm algorithm = null; 
		switch (inputParams.getRetrievalAlgorithm()) {
		case BASIC_ALGORITHM:
			algorithm = new BasicAlgorithm(docs);
			break;
		case IMPROVED_ALGORITHM:
			algorithm = new ImprovedAlgorithm(docs);
			break;
		}
		
		List<String> output = algorithm.cluster();
		if (output == null) {
			System.out.println("Faild to cluster the collection.");
			return;
		}
		
		// Output retrieval experiment results.
		Path outputPath = Paths.get(inputParams.getOutputFileName());
		try {
			Files.write(outputPath, output);
		} catch (IOException e) {
			System.out.println("Faild to write output file name: " + inputParams.getOutputFileName() + ".");
		}
		
	}

}
