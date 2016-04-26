package Clustering;

import java.util.List;

/**
 * Merges base clusters to produce final clusters using MST clustering. Not an
 * example of a great clustering algorithm, but a twist on the standard STC merging
 * algorithm of binary similarity.
 * 
 * @author harryross - harryross263@gmail.com.
 */
public class MSTMerger extends AbstractOverlappingClusterMerger {

	private final int numberOfClustersToFind;

	public MSTMerger(int numberOfClustersToFind) {
		this.numberOfClustersToFind = numberOfClustersToFind;
		// Set the minimum overlapDegree to be zero so that the initial base
		// cluster graph is constructed as a dense graph with pairwise edges 
		// between each individual base cluster in the graph.
		this.minOverlapDegree = 0;
	}

	@Override
	public List<Cluster> MergeClusters(List<Cluster> baseClustersToMerge) {
		// Generate the base cluster graph.
		List<ClusterInfo> baseClusterInfos = generateClusterInfo(baseClustersToMerge);
		ConnectClusters(baseClusterInfos);

		// Now that we have the base cluster graph, produce a MST on it.

		// Now that we have a MST on the base cluster graph, remove the
		// numberOfClustersToFind largest edges in order to leave
		// numberOfClustersToFind connected components (final clusters).
		return null;
	}

	public void setOverlapDegree(double overlapDegree_) {
		throw new UnsupportedOperationException("The MSTMerger relies on the minOverlapDegree" + "being zero.");
	};
}
