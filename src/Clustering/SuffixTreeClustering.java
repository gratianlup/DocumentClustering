package Clustering;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Kicks off the Suffix Tree (ST) clustering system and launches a GUI to view
 * the clusters formed.
 * 
 * @author harryross - harryross263@gmail.com.
 */
public class SuffixTreeClustering {

	/* The source from which to read the documents */
	private static IDocumentSource documentSource;

	/**
	 * Takes the filenames of the documents to be clustered as arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println(
					"Please provide the paths of files to cluster (i.e $ java SuffixTreeClustering file1 [file2+])");
			System.exit(0);
		}

		Queue<File> files = new ArrayDeque<>();
		for (String s : args) {
			files.add(new File(s));
		}

		documentSource = new ReutersSource(files);

		//List<Cluster> clusters = ClusterFinder.Find(documentSource, Integer.MAX_VALUE, 0, new MinDegreeClusterMerger(0.1));
		List<Cluster> clusters = ClusterFinder.Find(documentSource, Integer.MAX_VALUE, 0, new MSTMerger(1));

		System.out.println("Number of clusters found: " + clusters.size());

		for (Cluster c : clusters) {
			System.out.println(c.toString());
		}
	}
}
