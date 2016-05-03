package Clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Driver code for the Suffix Tree (ST) clustering system.
 *
 * @author harryross - harryross263@gmail.com.
 */
public class SuffixTreeClustering {

	private static final int n_clusters = 130;
	private static int n_documents = 5000;
	
	/**
	 * Takes the filenames of the documents to be clustered as arguments.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(
					"Please provide the top level directory (i.e $ java SuffixTreeClustering \"~/Reuters/\")");
			System.exit(0);
		}

		File folder = new File(args[0]);
		
		try{
			n_documents = Integer.parseInt(args[1]);
		} catch (IndexOutOfBoundsException e) {
			// Fall through.
		}

		if (!folder.exists()) {
			throw new RuntimeException(folder.getAbsolutePath() + " doesn't exist.");
		}

		//IDocumentSource minDegreeSource = new ReutersSource(folder);
		IDocumentSource mstSource = new ReutersSource(folder, n_documents);

		//Set<Cluster> minDegreeClusters = ClusterFinder.Find(minDegreeSource, Integer.MAX_VALUE, 0, new MinDegreeClusterMerger(0.99));
		Set<Cluster> mstClusters = ClusterFinder.Find(mstSource, Integer.MAX_VALUE, 5, new MSTMerger(n_clusters));

		//System.out.println("Number of clusters found using minDegree: " + minDegreeClusters.size());
		System.out.println("Number of clusters found using MST: " + mstClusters.size());

		List<Double> accuracies = new ArrayList<>(n_clusters);
		for (Cluster c : mstClusters) {
			accuracies.add(findAccuracy(c, mstSource));
		}
		
		OptionalDouble average = accuracies
	            .stream()
	            .mapToDouble(a -> a)
	            .average();
		
		System.out.println("Average accuracy across " + n_clusters + " clusters is: " + average.orElse(0));
		
	}
	
	public static double findAccuracy(Cluster cluster, IDocumentSource source) {		
		List<Article> clusterArticles = cluster.articles(source.articles());
		
		Map<String, Integer> topic_count = new HashMap<>();
		for (Article article : clusterArticles) {
			for (String topic : article.topics()) {
				if (topic_count.get(topic) == null) {
					topic_count.put(topic, 1);
				} else {
					topic_count.put(topic, topic_count.get(topic) + 1);
				}
			}
		}
		
		double max_occurence = -1;
		for (String s : topic_count.keySet()) {
			if (topic_count.get(s) > max_occurence) {
				max_occurence = topic_count.get(s);
			}
		}
		
		return max_occurence / (double) clusterArticles.size();
	}
}
