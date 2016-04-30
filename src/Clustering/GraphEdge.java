package Clustering;

import Clustering.AbstractOverlappingClusterMerger.GraphVertex;

/**
 * Represents an edge in a weighted graph. An unweighted edge
 * can simply be a GraphEdge with weight equal to zero.
 * 
 * @author harryross - harryross263@gmail.com.
 */
public class GraphEdge implements Comparable<GraphEdge> {
	
	/* The vertices at the start and end of this edge */
	private GraphVertex a;
	private GraphVertex b;
	
	/* The weight of the edge */
	private double weight;
	
	public GraphEdge(GraphVertex a, GraphVertex b, double weight) {
		this.a = a;
		this.b = b;
		this.weight = weight;
	}
	
	public GraphVertex getA() {
		return this.a;
	}
	
	public GraphVertex getB() {
		return this.b;
	}
	
	public GraphVertex getOther(GraphVertex current) {
		if (current.equals(a)) {
			return b;
		}
		return a;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public void reweight(double newWeight) {
		this.weight = newWeight;
	}
	
	public int compareTo(GraphEdge other) {
		return (int) (other.weight - this.weight);
	}
}
