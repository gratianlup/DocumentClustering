package Clustering;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractOverlappingClusterMerger implements IClusterMerger {

	/**
	 * The degree to which the number of documents in a base cluster must
	 * overlap before the two base clusters can be merged into one cluster.
	 */
	double minOverlapDegree;

	/**
	 * Returns a list of {@link GraphVertex} objects describing the list of base
	 * clusters passed to it.
	 * 
	 * @param baseClusters
	 *            - clusters for which to generate info.
	 * @return
	 */
	public Set<GraphVertex> generateVertices(Set<Cluster> baseClusters) {
		Set<GraphVertex> vertices = new HashSet<>();
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

		public GraphVertex(Cluster cluster) {
			this.cluster = cluster;
		}

		public Cluster cluster() {
			return cluster;
		}
	}
}
