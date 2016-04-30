package Clustering;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import Clustering.AbstractOverlappingClusterMerger.GraphVertex;

/**
 * Represents a graph of {@link Cluster} objects. Useful functions include
 * {@link #getUniqueEdges()} to deduplicate incoming/outgoing edges as well as
 * {@link #getConnectedComponents()} and {@link #buildMST(ClusterGraph)}.
 * 
 * @author harryross -- harryross263@gmail.com.
 * 
 */
public class ClusterGraph {

	private Set<GraphVertex> vertices;

	/* Adjacency map of vertices to the edges coming in or out of them. 
	 * Edges are undirected. */
	private Map<GraphVertex, Set<GraphEdge>> edges;

	public ClusterGraph(Set<GraphVertex> vertices, Map<GraphVertex, Set<GraphEdge>> edges) {
		this.vertices = vertices;
		this.edges = edges;
	}

	public Set<GraphVertex> vertices() {
		return this.vertices;
	}

	public Map<GraphVertex, Set<GraphEdge>> edges() {
		return this.edges;
	}

	/**
	 * Repeatedly performs a BFS finding connected components as it goes. In the worst
	 * case this will return a copy of the entire ClusterGraph.
	 * 
	 * @param v
	 * @return
	 */
	public Set<ClusterGraph> getConnectedComponents() {
		Set<ClusterGraph> connectedComponents = new HashSet<>();
		
		Queue<GraphVertex> unvisitedVertices = new ArrayDeque<>();
		unvisitedVertices.addAll(vertices);
		
		while (!unvisitedVertices.isEmpty()) {
			// Start our next BFS from vertex v.
			GraphVertex v = unvisitedVertices.poll();
			
			// Build up a collection of the vertices and edges that we encounter along
			// the BFS.
			Set<GraphVertex> connectedVertices = new HashSet<>();
			Map<GraphVertex, Set<GraphEdge>> connectedEdges = new HashMap<>();	
			
			// Utility data structures to keep track of which vertices we've seen on
			// our path, and which vertices are yet to be processed.
			Set<GraphVertex> visited = new HashSet<>();
			Queue<GraphVertex> fringe = new ArrayDeque<>();
			fringe.add(v);

			while (!fringe.isEmpty()) {
				GraphVertex gv = fringe.poll();

				if (visited.contains(gv)) {
					// We've already seen this vertex and added its
					// neighbors to the fringe, so ignore it now.
					continue;
				}
				
				// Process the current vertex i.e. mark as visited and add it
				// to the connected component that we're currently building up.
				visited.add(gv);
				connectedVertices.add(gv);
				connectedEdges.put(gv, edges.get(gv));

				for (GraphEdge ge : edges.get(gv)) {
					// Add the vertex at the other end of each edge to the fringe.
					fringe.offer(ge.getOther(gv));
				}
			}
			
			// Remove the vertices that are in the connected component that we just discovered
			// so that the over arching while loop will eventually terminate.
			unvisitedVertices.removeAll(connectedVertices);
			connectedComponents.add(new ClusterGraph(connectedVertices, connectedEdges));
		}
		
		return connectedComponents;
	}
	
	/**
	 * Returns a minimum spanning tree across cg. Uses
	 * Kruskal's algorithm to merge forests.
	 * 
	 * @return
	 */
	public static ClusterGraph buildMST(ClusterGraph cg) {
		Map<GraphVertex, Set<GraphEdge>> mstEdges = new HashMap<>();
		
		// Construct a list of vertices in order to index the vertices in
		// this graph against the union find.
		List<GraphVertex> verticeList = new ArrayList<>();
		verticeList.addAll(cg.vertices());
		
		// Priority queue to terminate the construction of the MST, 
		// and union find to determine when components are connected.
		Queue<GraphEdge> pq = new PriorityQueue<>();
		pq.addAll(cg.getUniqueEdges());
		UF uf = new UF(verticeList.size());
		
		while (!pq.isEmpty()) {
			GraphEdge edge = pq.poll();
			int v = verticeList.indexOf(edge.getA());
			int w = verticeList.indexOf(edge.getB());
			
			if (!uf.connected(v, w)) {
				uf.union(v, w);
				addEdge(edge.getA(), edge, mstEdges);
				addEdge(edge.getB(), edge, mstEdges);
			}
		}
		
		// The MST will have the same vertex set, but will only contain the edges found
		// using the algorithm above.
		return new ClusterGraph(cg.vertices(), mstEdges);
	}
	
	/**
	 * Removes the numEdgesToRemove largest edges from cg. Used to separate a MST into 
	 * numEdgesToRemove + 1 different connected components.
	 * 
	 * @param numEdgesToRemove
	 * @return
	 */
	public static ClusterGraph removeLargestEdges(ClusterGraph cg, int numEdgesToRemove) {
		// We use a list here so that we can sort the edges by weight and index
		// the heaviest/lightest etc.
		List<GraphEdge> uniqueEdges = new ArrayList<>();
		uniqueEdges.addAll(cg.getUniqueEdges());
		Collections.sort(uniqueEdges);
		
		// Grab the numEdgesToRemove largest edges from the sorted list of unique edges.
		int numUniqueEdges = uniqueEdges.size();
		List<GraphEdge> edgesToRemove = uniqueEdges.subList(numUniqueEdges - numEdgesToRemove, numUniqueEdges);
		
		// Remove the appropriate edges from any entry in cg's adjacency map.
		for(Set<GraphEdge> edges : cg.edges().values()) {
			// Must use an iterator here since we will be modifying the data structure.
			for (Iterator<GraphEdge> j = edges.iterator(); j.hasNext();) {
				GraphEdge edge = j.next();
				if (edgesToRemove.contains(edge)) {
					j.remove();
				}
			}
		} 
		
		return cg;
	}

	/**
	 * Returns a set of the clusters that are contained in this cluster graph.
	 * @return
	 */
	public Set<Cluster> getClusters() {
		Set<Cluster> clusters = new HashSet<>();
		
		for (GraphVertex v : vertices) {
			clusters.add(v.cluster());
		}
		
		return clusters;
	}
	
	/**
	 * Returns a collection of the unique edges in this graph. Since edges will be
	 * added to the adjacency map in both directions (i.e. from a -> b and b -> a), 
	 * this method returns only the unique ones.
	 * 
	 * @return
	 */
	public Set<GraphEdge> getUniqueEdges() {
		Set<GraphEdge> uniqueEdges = new HashSet<>();
		
		for (Set<GraphEdge> edgeSet : edges.values()) {
			uniqueEdges.addAll(edgeSet);
		}
		
		return uniqueEdges;
	}

	/**
	 * Adds e to the adjacency map entry for v. If no entry exists then a new
	 * one is created and e is added to that.
	 * 
	 * @param v
	 * @param e
	 * @param adjacencyMap
	 */
	public static void addEdge(GraphVertex v, GraphEdge e, Map<GraphVertex, Set<GraphEdge>> adjacencyMap) {
		try {
			adjacencyMap.get(v).add(e);
		} catch (NullPointerException exception) {
			// The vertex v doesn't have any adjacent edges yet, so construct a new 
			// adjacency set and enter it into the adjacency map.
			Set<GraphEdge> edges = new HashSet<>();
			edges.add(e);
			adjacencyMap.put(v, edges);
		}
	}

	/**
	 * Creates a new ClusterGraph with edges between each vertex that is less
	 * than minSimilarity apart.
	 * 
	 * @param vertices
	 * @param minSimilarity
	 * @return
	 */
	public static ClusterGraph buildGraph(Set<GraphVertex> vertices, double minSimilarity) {
		Map<GraphVertex, Set<GraphEdge>> edges = new HashMap<>();

		for (GraphVertex v : vertices) {
			for (GraphVertex j : vertices) {
				if (v.equals(j)) {
					// We don't allow for individual incidence in the cluster graph.
					continue;
				}

				// Similarity is symmetric, so only need to check one way.
				double similarity = v.cluster().similarity(j.cluster());
				if (similarity >= minSimilarity) {
					// If the similarity between these two clusters is larger than
					// the minimum similarity specified, then let's connect them by
					// constructing an edge between the two wrapper vertices and adding
					// it to each of their adjacency sets.
					GraphEdge edge = new GraphEdge(v, j, 1 - similarity);
					addEdge(v, edge, edges);
					addEdge(j, edge, edges);
				}
			}
		}

		return new ClusterGraph(vertices, edges);
	}
}
