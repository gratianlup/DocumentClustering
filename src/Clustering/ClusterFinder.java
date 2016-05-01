// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "DocumentClustering" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "DocumentClustering" nor
// may "DocumentClustering" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package Clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClusterFinder {

	private static DocumentReader reader;

	// Function used for unit tests.
	public static SuffixTree.Node ParseSource(IDocumentSource source) {
		DocumentReader reader = new DocumentReader(source);
		reader.Read();
		return reader.Tree().Root();
	}

	/**
	 * Returns a list with all clusters from the document that meet conditions
	 * specified in the parameters.
	 *
	 * @param source
	 *            The source from where to read the documents.
	 * @param clusterOverlapDegree
	 *            The minimum overlapping degree for two clusters to be combined
	 *            into a single one.
	 * @param maxClusters
	 *            The maximum number of clusters to add to the result lists. The
	 *            rest of the documents are added to a cluster named "Other".
	 * @param minClusterWeight
	 *            The minimum weight of a cluster to be considered.
	 * @return A list with all clusters meeting the specified conditions.
	 */
	public static Set<Cluster> Find(IDocumentSource source, int maxClusters,
			double minClusterWeight, IClusterMerger merger) {
		assert(source != null);
		assert(maxClusters > 0);
		// ------------------------------------------------
		// Read all documents and get the base clusters.
		// The weight of each one is computed and is used to sort them
		// in ascending order. The first maxClusters clusters are created by
		// merging base clusters. Any base clusters that haven't been merged
		// into a cluster are merged into an aggregate final cluster labeled
		// 'Other'.
		reader = new DocumentReader(source);
		reader.Read();
		System.out.println("Finished reading from this source.");
		Set<Cluster> baseClusterSet = reader.GetBaseClusters(minClusterWeight);

		if (baseClusterSet.isEmpty()) {
			System.out.println("No base clusters were found, this indicates an error in the program.");
			return new HashSet<Cluster>();
		}

		// Select the first 'maxClusters' base clusters.
		List<Cluster> baseClusterList = new ArrayList<>();
		baseClusterList.addAll(baseClusterSet);
		Collections.sort(baseClusterList);
		int limit = Math.min(maxClusters, baseClusterList.size());

		baseClusterSet.clear();
		baseClusterSet.addAll(baseClusterList.subList(0, limit));
		Set<Cluster> finalClusters = merger.MergeClusters(baseClusterSet);

		if (limit < baseClusterSet.size()) {
			// Some base clusters remained, group them under a single cluster.
			Cluster other = Cluster.Merge(baseClusterList.subList(limit, baseClusterList.size()));
			other.SetLabel("Other");
			finalClusters.add(other);
		}

		return finalClusters;
	}

	public static DocumentReader getReader() {
		return reader;
	}
}
