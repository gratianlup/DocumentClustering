package Clustering;

import java.io.File;
import java.util.Set;

/**
 * Driver code for the Suffix Tree (ST) clustering system.
 *
 * @author harryross - harryross263@gmail.com.
 */
public class SuffixTreeClustering {

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

		if (!folder.exists()) {
			throw new RuntimeException(folder.getAbsolutePath() + " doesn't exist.");
		}

		IDocumentSource minDegreeSource = new ReutersSource(folder);
		IDocumentSource mstSource = new ReutersSource(folder);

		Set<Cluster> minDegreeClusters = ClusterFinder.Find(minDegreeSource, Integer.MAX_VALUE, 0, new MinDegreeClusterMerger(0.99));
		Set<Cluster> mstClusters = ClusterFinder.Find(mstSource, Integer.MAX_VALUE, 0.0, new MSTMerger(10));

		System.out.println("Number of clusters found using minDegree: " + minDegreeClusters.size());
		System.out.println("Number of clusters found using MST: " + mstClusters.size());
	}
}
