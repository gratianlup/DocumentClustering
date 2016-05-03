package Clustering;

import java.util.HashSet;
import java.util.Set;

/**
 * Merges base clusters to produce final clusters using MST clustering. Not an
 * example of a great clustering algorithm, but a twist on the standard STC
 * merging algorithm of binary similarity.
 *
 * @author harryross - harryross263@gmail.com.
 */
public class MSTMerger extends AbstractOverlappingClusterMerger {

	private final int numberOfEdgesToRemove;

	public MSTMerger(int numberOfClustersToFind) {
		// The number of edges that we must remove from a spanning tree in order
		// to be left with K unique connected components is equal to K - 1.
		this.numberOfEdgesToRemove = numberOfClustersToFind - 1;
		// Set the minimum overlapDegree to be zero so that the initial base
		// cluster graph is constructed as a dense graph with pairwise edges
		// between each individual base cluster in the graph.
		this.minOverlapDegree = 0.0;
	}

	@Override
	public Set<Cluster> MergeClusters(Set<Cluster> baseClustersToMerge) {
		System.out.println("Merging clusters");
		// Generate the base cluster graph.
		System.out.println("Generating " + baseClustersToMerge.size() + " graph vertices...");
		Set<GraphVertex> vertices = generateVertices(baseClustersToMerge);
		System.out.println("Building cluster graph...");
		ClusterGraph cg = ClusterGraph.buildGraph(vertices, this.minOverlapDegree);

		// Now that we have the base cluster graph, produce a MST on it.
		System.out.println("Building the MST...");
		ClusterGraph mst = ClusterGraph.buildMST(cg);
		System.out.println("Number of unique edges in MST: " + mst.getUniqueEdges().size());

		// Now that we have a MST on the base cluster graph, remove the
		// numberOfClustersToFind - 1 largest edges in order to leave
		// numberOfClustersToFind connected components (final clusters).
		System.out.println("Removing the " + numberOfEdgesToRemove + " largest edges in the MST...");
		ClusterGraph finalGraph = ClusterGraph.removeLargestEdges(mst, numberOfEdgesToRemove);
		System.out.println("Number of unique edges in finalGraph: " + finalGraph.getUniqueEdges().size());

		// Now that we have the final cluster graph, we need to merge the clusters represented in
		// each different component into their own cluster.
		System.out.println("Finding the connected components in the final graph...");
		Set<ClusterGraph> connectedComponents = finalGraph.getConnectedComponents();
		System.out.println("Number of connected components found: " + connectedComponents.size());

		Set<Cluster> finalClusters = new HashSet<>();
		System.out.println("Merging the clusters within each connected component...");
		for (ClusterGraph connectedComponent : connectedComponents) {
			finalClusters.add(Cluster.Merge(connectedComponent.getClusters()));
		}

		return finalClusters;
	}

	public void setOverlapDegree(double overlapDegree_) {
		throw new UnsupportedOperationException("The MSTMerger relies on the minOverlapDegree" + "being zero.");
	};
}
