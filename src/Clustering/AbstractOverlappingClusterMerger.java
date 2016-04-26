package Clustering;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOverlappingClusterMerger implements IClusterMerger {
	
	/**
	 * The degree to which the number of documents in a base cluster must overlap
	 * before the two base clusters can be merged into one cluster. 
	 */
	double minOverlapDegree;

	/**
	 * Calculates the pairwise distance between clusters and connects them with
	 * an edge if the distance between them is less than overlapDegree_.
	 * 
	 * Distance is defined as the average overlap between the document members
	 * of the cluster.
	 */
	void ConnectClusters(List<ClusterInfo> baseClusterInfo) {
		// Compare all pairs of clusters and add an edge
		// between the ones that are similar.
		int count = baseClusterInfo.size();

		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				if (i == j) {
					// Don't compare a document with itself.
					continue;
				}

				ClusterInfo a = baseClusterInfo.get(i);
				ClusterInfo b = baseClusterInfo.get(j);

				double similarity = a.Cluster().similarity(b.Cluster());
				if (similarity > minOverlapDegree) {
					// The documents are similar enough, connect them.
					a.Edges().add(new ClusterInfoEdge(b, 1 - similarity));
				}
			}
		}
	}
	
	/**
	 * Returns a list of {@link ClusterInfo} objects describing the list of
	 * base clusters passed to it.
	 * @param baseClusters - clusters for which to generate info.
	 * @return
	 */
	public List<ClusterInfo> generateClusterInfo(List<Cluster> baseClusters) {
		List<ClusterInfo> clusterInfos = new ArrayList<ClusterInfo>();
		for (Cluster bc : baseClusters) {
			clusterInfos.add(new ClusterInfo(bc));
		}
		
		return clusterInfos;
	}
	
	public void setOverlapDegree(double overlapDegree_) {
		this.minOverlapDegree = overlapDegree_;
	}

	class ClusterInfo {
		private Cluster cluster;
		private boolean discovered;
		private ArrayList<ClusterInfoEdge> edges;

		public ClusterInfo(Cluster cluster) {
			this.cluster = cluster;
			this.edges = new ArrayList<ClusterInfoEdge>();
		}

		public Cluster Cluster() {
			return cluster;
		}

		public boolean Discovered() {
			return discovered;
		}

		public void SetDiscovered(boolean value) {
			discovered = value;
		}

		public List<ClusterInfoEdge> Edges() {
			return edges;
		}
	}
	
	class ClusterInfoEdge implements Comparable<ClusterInfoEdge> {
		/* The ClusterInfo that this edge connects to. */
		private ClusterInfo other;
		
		/* The weight of this edge. i.e. 1 - the similarity of the clusters
		 * at either end of the edge. */
		private double weight;
		
		public ClusterInfoEdge(ClusterInfo other, double weight) {
			this.other = other;
			this.weight = weight;
		}
		
		public ClusterInfo getOther() {
			return this.other;
		}
		
		public double getWeight() {
			return this.weight;
		}
		
		public int compareTo(ClusterInfoEdge other) {
			return (int) (other.weight - weight);
		}
	}
}
