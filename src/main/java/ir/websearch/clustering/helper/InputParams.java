package ir.websearch.clustering.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InputParams {

	private final String outputFileName;
	private final String retrievalAlgorithm;
	private final Integer k;
	private final String rootDirectory;

	public String getOutputFileName() {
		return outputFileName;
	}

	public String getRetrievalAlgorithm() {
		return retrievalAlgorithm;
	}
	
	public Integer getK() {
		return k;
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public static class Parser {

		private static final String RETRIEVAL_ALGORITHM_KEY = "retrievalAlgorithm";
		private static final String OUTPUT_FILE_KEY = "outputFile";
		private static final String K_KEY = "k";
		private static final String ROOT_DIRECTORY_KEY = "rootDirectory";
		
		private String outputFileName = null;
		private String retrievalAlgorithm = null;
		private Integer k = null;
		private String rootDirectory = null;
		
		private String fileName;

		public Parser(String fileName) {
			this.fileName = fileName;
		}

		public InputParams parse() {
			InputParams inputParams = null;
			Path parameterFilePath = Paths.get(this.fileName);
			try {
				List<String> parameterLines = Files.readAllLines(parameterFilePath);
				for (String paramLine : parameterLines) {
					if (retrievalAlgorithm == null) {
						retrievalAlgorithm = getParamByKey(paramLine, RETRIEVAL_ALGORITHM_KEY);
					}
					
					if (outputFileName == null) {
						outputFileName = getParamByKey(paramLine, OUTPUT_FILE_KEY);
					}
					
					if (k == null) {
						String k_str = getParamByKey(paramLine, K_KEY);
						if (org.apache.commons.lang3.StringUtils.isNotEmpty(k_str)) {
							k = Integer.parseInt(k_str);
						}
					}
					
					if (this.rootDirectory == null) {
						this.rootDirectory = getParamByKey(paramLine, ROOT_DIRECTORY_KEY);
					}
				}
				
				if (isInputValid()) {
					inputParams = new InputParams(this);
				}				
			} catch (IOException e) {
				
			}
								
			return inputParams;
		}
		
		private static String getParamByKey(String line, String paramKey) {
			String param = null;
			String paramKeyPrefix = paramKey + "="; 
			if (line.startsWith(paramKeyPrefix)) {
				param = line.substring(line.indexOf(paramKeyPrefix) + paramKeyPrefix.length(), line.length());
			}
			
			return param;
		}
		
		private boolean isInputValid() {
			boolean retval = false;
			
			if (this.k != null && this.fileName != null && this.outputFileName != null && this.rootDirectory != null && this.retrievalAlgorithm != null) {
				retval = true;
			}
			
			return retval;
		}
	}

	private InputParams(Parser parser) {
		this.outputFileName = parser.outputFileName;
		this.retrievalAlgorithm = parser.retrievalAlgorithm;
		this.k = parser.k;
		this.rootDirectory = parser.rootDirectory;
	}
}
