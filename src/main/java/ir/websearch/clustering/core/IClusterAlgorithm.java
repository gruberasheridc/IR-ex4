package ir.websearch.clustering.core;

import java.util.List;

public interface IClusterAlgorithm {
	
	/**
	 * The method performers clustering for the given document collection. 
	 * @return clustering results in printable formated lines (DocID, ClusterNumber).
	 */
	public List<String> cluster();

}
