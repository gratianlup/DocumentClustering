package Clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import Clustering.AbstractOverlappingClusterMerger.GraphVertex;

public class MST {
	
	/* The weight of the MST. */
	private double weight;
	
	/* The edges in the MST. */
	private List<GraphEdge<GraphVertex>> mst = new ArrayList<>();
	
	public MST(final List<GraphVertex> G /* Graph to find a MST on. */) {
		// Deduplicate the edges in the graph.
		Set<GraphEdge<GraphVertex>> edgesInGraph = new HashSet<>();
		for (GraphVertex gv : G) {
			edgesInGraph.addAll(gv.edges());
		}
		
		Queue<GraphEdge<GraphVertex>> pq = new PriorityQueue<>();
		pq.addAll(edgesInGraph);
		
		UF uf = new UF(G.size());
		while (!pq.isEmpty() && mst.size() < G.size() - 1) {
			GraphEdge<GraphVertex> edge = pq.poll();
			int v = G.indexOf(edge.getStart());
			int w = G.indexOf(edge.getEnd());
			
			if (!uf.connected(v, w)) {
				uf.union(v, w);
				mst.add(edge);
				weight += edge.getWeight();
			}
		}
	}
	
	public double weight() {
		return this.weight;
	}
	
	public List<GraphEdge<GraphVertex>> mst() {
		return this.mst;
	}
}
