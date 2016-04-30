package Clustering;

/**
 * Represents an edge in a weighted graph. An unweighted edge
 * can simply be a GraphEdge with weight equal to zero.
 * 
 * @author harryross - harryross263@gmail.com.
 */
public class GraphEdge<T> implements Comparable<GraphEdge<T>>{
	
	/* The vertices at the start and end of this edge */
	private T start;
	private T end;
	
	/* The weight of the edge */
	private double weight;
	
	public GraphEdge(T start, T end, double weight) {
		this.start = start;
		this.end = end;
		this.weight = weight;
	}
	
	public T getStart() {
		return this.start;
	}
	
	public T getEnd() {
		return this.end;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public void reweight(double newWeight) {
		this.weight = newWeight;
	}
	
	public int compareTo(GraphEdge<T> other) {
		return (int) (other.weight - this.weight);
	}
}
