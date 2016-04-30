package Clustering;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOverlappingClusterMerger implements IClusterMerger {

	/**
	 * The degree to which the number of documents in a base cluster must
	 * overlap before the two base clusters can be merged into one cluster.
	 */
	double minOverlapDegree;

	/**
	 * Calculates the pairwise distance between clusters and connects them with
	 * an edge if the distance between them is less than overlapDegree_.
	 * 
	 * Distance is defined as the average overlap between the document members
	 * of the cluster.
	 */
	void ConnectClusters(List<GraphVertex> graph) {
		// Compare all pairs of base clusters and add an edge
		// between the ones that are similar.
		int count = graph.size();

		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				if (i == j) {
					// Don't compare a document with itself.
					continue;
				}

				GraphVertex a = graph.get(i);
				GraphVertex b = graph.get(j);

				double similarity = a.cluster().similarity(b.cluster());
				if (similarity > minOverlapDegree) {
					// The documents are similar enough, so connect them.
					GraphEdge<GraphVertex> forwardEdge = new GraphEdge<GraphVertex>(a, b, 1 - similarity);
					GraphEdge<GraphVertex> backwardEdge = new GraphEdge<GraphVertex>(b, a, 1 - similarity);
					
					a.edges().add(forwardEdge);
					b.edges().add(backwardEdge);
				}
			}
		}
	}

	/**
	 * Returns a list of {@link GraphVertex} objects describing the list of base
	 * clusters passed to it.
	 * 
	 * @param baseClusters
	 *            - clusters for which to generate info.
	 * @return
	 */
	public List<GraphVertex> generateVertices(List<Cluster> baseClusters) {
		List<GraphVertex> vertices = new ArrayList<GraphVertex>();
		for (Cluster bc : baseClusters) {
			vertices.add(new GraphVertex(bc));
		}

		return vertices;
	}

	public void setOverlapDegree(double overlapDegree_) {
		this.minOverlapDegree = overlapDegree_;
	}

	class GraphVertex {
		/* The cluster that this vertex belongs to. */
		private Cluster cluster;
		
		/* Whether or not this vertex has been discovered. Used to
		 * keep track of a BFS. */
		private boolean discovered;
		
		private ArrayList<GraphEdge<GraphVertex>> edges;

		public GraphVertex(Cluster cluster) {
			this.cluster = cluster;
			this.edges = new ArrayList<GraphEdge<GraphVertex>>();
		}

		public Cluster cluster() {
			return cluster;
		}

		public boolean isDiscovered() {
			return discovered;
		}

		public void setDiscovered(boolean value) {
			discovered = value;
		}

		public List<GraphEdge<GraphVertex>> edges() {
			return edges;
		}
	}
}
