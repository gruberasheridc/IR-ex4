package ir.websearch.clustering.doc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import ir.websearch.clustering.doc.Document.Builder;
import ir.websearch.clustering.helper.StringUtils;

public class DocumentsParser {
	
	private final String rootDirectory;
	
	public DocumentsParser(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	public Collection<Document> parse() {
		Collection<Document> documents = new ArrayList<Document>();

		// Parse and collect all documents. Assuming that the first hierarchy after the root directory are the clusters.
		try {
			Files.walk(Paths.get(rootDirectory)).forEach(folderPath -> {
				if (Files.isDirectory(folderPath) && !rootDirectory.equals(folderPath.getFileName().toString())) {
					// New cluster to process. Parse all files of the cluster to Documents.
					try {
						Files.walk(folderPath).forEach(filePath -> {
							if (Files.isRegularFile(filePath)) {
								try {
									List<String> fileLines = Files.lines(filePath, StandardCharsets.ISO_8859_1)
											.filter(line -> org.apache.commons.lang3.StringUtils.isNotBlank(line))
											.collect(Collectors.toList());
									
									if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fileLines)) {
							    		Document.Builder docBuilder = new Builder();
								    	String fileName = FilenameUtils.removeExtension(filePath.getFileName().toString());
										docBuilder.fileNum(fileName);
																				
										// The folders with the first hierarchy after the root directory are the clusters.
										String clusterName = folderPath.getFileName().toString();
										docBuilder.origCluster(clusterName);
										
										String docId = clusterName + fileName;
										docBuilder.docId(docId);
								    	
								    	String text = String.join(" ", fileLines);								    	
								    	text = StringUtils.removeRedundantChars(text, StringUtils.REMOVE_CHARS_REGEX);
								    	text = StringUtils.removeShortWords(text, StringUtils.SHORT_WORD_REGEX);
								    	text = StringUtils.whitespacesToSingleSpace(text);
								    	docBuilder.text(text);
								    	
								    	documents.add(docBuilder.build());
							    	}
								} catch (Exception e) {
									e.printStackTrace();
								}
						    }						
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			    
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return documents;
	}
	
}
