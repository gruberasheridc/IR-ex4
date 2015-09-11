package ir.websearch.clustering.doc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import ir.websearch.clustering.doc.Document.Builder;

public class DocumentsParser {
	
	private static final String BUSINESS_FOLDER = "business";
	private static final String ENTERTAINMENT_FOLDER = "entertainment";
	private static final String POLITICS_FOLDER = "politics";
	private static final String SPORT_FOLDER = "sport";
	private static final String TECH_FOLDER = "tech";	
	
	private static final String REMOVE_CHARS_REGEX = "[^-A-Za-z0-9\\s]";
	
	private final String rootDirectory;
	
	public DocumentsParser(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	public Collection<Document> parse() {
		Collection<Document> documents = new ArrayList<Document>();

		// Parse and collect all the business files.
		String businessPath = rootDirectory + File.separator + BUSINESS_FOLDER;
		try {
			Files.walk(Paths.get(businessPath)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
					try {
						List<String> fileLines = Files.lines(filePath)
								.filter(line -> org.apache.commons.lang3.StringUtils.isNotBlank(line))
								.collect(Collectors.toList());
						
						if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fileLines)) {
				    		Document.Builder docBuilder = new Builder();
					    	String fileName = FilenameUtils.removeExtension(filePath.getFileName().toString());
							docBuilder.fileNum(fileName);
							docBuilder.origCluster(BUSINESS_FOLDER);
					    	docBuilder.title(fileLines.get(0)); // First line is the title.
					    	
					    	String text = String.join(" ", fileLines);
					    	docBuilder.text(text);
					    	
					    	documents.add(docBuilder.build());
				    	}
					} catch (Exception e) {}
			    }
			});
		} catch (IOException e) {
			
		}
		
		return documents;
	}

}
