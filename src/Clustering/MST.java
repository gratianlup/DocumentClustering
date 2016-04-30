package Clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import Clustering.AbstractOverlappingClusterMerger.GraphVertex;

public class MST {
	
	/* The weight of the MST. */
	private double weight;
	
	/* The edges in the MST. */
	private List<GraphEdge> mst = new ArrayList<>();
	
	public MST(Set<GraphVertex> G /* Graph to find a MST on. */) {
		// Add all of the edges in the graph.
		
		Queue<GraphEdge> pq = new PriorityQueue<>();
		int numEdges = 0;
		for (GraphVertex gv : G) {
			numEdges += gv.edges().size();
			pq.addAll(gv.edges().values());
		}
		
		System.out.println("Num edges: " + numEdges);
		System.out.println("Num vertices: " + G.size());
		System.out.println("Expecting: " + ((G.size()*(G.size() - 1))/2));
		
		UF uf = new UF(G.size());
		while (!pq.isEmpty() && mst.size() < G.size() - 1) {
			GraphEdge edge = pq.poll();
			int v = G.indexOf(edge.getA());
			int w = G.indexOf(edge.getB());
			
			if (!uf.connected(v, w)) {
				uf.union(v, w);
				mst.add(edge);
				weight += edge.getWeight();
			}
		}
		
		verifyConnected(uf, G);
	}
	
	public double weight() {
		return this.weight;
	}
	
	public List<GraphEdge> mst() {
		return this.mst;
	}
	
	private void verifyConnected(UF uf, List<GraphVertex> G) {
		for (int i = 0; i < G.size(); i++) {
			for (int j = i; j < G.size(); j++) {
				if (!uf.connected(i, j)) {
					throw new RuntimeException();
				}
			}
		}
		System.out.println("MST is connected.");
	}
}
