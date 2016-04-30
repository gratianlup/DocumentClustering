package Clustering;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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
		this.numberOfEdgesToRemove = numberOfClustersToFind - 1;
		// Set the minimum overlapDegree to be zero so that the initial base
		// cluster graph is constructed as a dense graph with pairwise edges 
		// between each individual base cluster in the graph.
		this.minOverlapDegree = 0;
	}

	@Override
	public List<Cluster> MergeClusters(List<Cluster> baseClustersToMerge) {
		// Generate the base cluster graph.
		System.out.println("Number of base clusters to merge: " + baseClustersToMerge.size());
		List<GraphVertex> vertices = generateVertices(baseClustersToMerge);
		ConnectClusters(vertices);

		// Now that we have the base cluster graph, produce a MST on it.
		System.out.println("Number of vertices: " + vertices.size());
		MST mst = new MST(vertices);

		// Now that we have a MST on the base cluster graph, remove the
		// numberOfClustersToFind largest edges in order to leave
		// numberOfClustersToFind connected components (final clusters).
		List<GraphEdge<GraphVertex>> finalEdges = mst.mst();
		System.out.println("Number of edges in the MST: " + finalEdges.size());
		finalEdges = finalEdges.subList(0, finalEdges.size() - numberOfEdgesToRemove);
		
		// Now that we know the edges that will be in the final cluster graph,
		// find the base clusters that they connect and merge them to produce
		// a list of final clusters.
		ArrayList<ArrayList<Cluster>> connectedComponents = new ArrayList<>();
		
		for (int i = 0; i < finalEdges.size(); i++) {
			GraphVertex v = finalEdges.get(i).getStart();
			
			if (v.isDiscovered()) {
				// We have already discovered the connected component that
				// this vertex belongs to.
				continue;
			}
			
			ArrayList<Cluster> graphComponent = new ArrayList<>();
			connectedComponents.add(graphComponent);
			
			Queue<GraphVertex> q = new ArrayDeque<>();
			q.offer(v);
			v.setDiscovered(true);
			FindComponent(q, graphComponent);
		}
		
		ArrayList<Cluster> finalClusters = new ArrayList<>(connectedComponents.size());
		
		for (ArrayList<Cluster> c : connectedComponents) {
			finalClusters.add(Cluster.Merge(c));
		} 
		
		return finalClusters;
	}
	
	// Finds a connected component. It is presumed that the start node
		// is already found in the specified queue.
		private void FindComponent(Queue<GraphVertex> queue, ArrayList<Cluster> component) {
			while (queue.size() > 0) {
				GraphVertex info = queue.poll();
				component.add(info.cluster());

				// Add all neighbor nodes to the component.
				for (int i = 0; i < info.edges().size(); i++) {
					GraphVertex next = info.edges().get(i).getEnd();

					if (!next.isDiscovered()) {
						next.setDiscovered(true);
						queue.add(next);
					}
				}
			}
		}

	public void setOverlapDegree(double overlapDegree_) {
		throw new UnsupportedOperationException("The MSTMerger relies on the minOverlapDegree" + "being zero.");
	};
}
