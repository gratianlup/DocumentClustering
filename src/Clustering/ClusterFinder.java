// Copyright (c) Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
//       copyright notice, this list of conditions and the following
//       disclaimer in the documentation and/or other materials provided
//       with the distribution.
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
import java.util.List;

public final class ClusterFinder {
    // Function used for unit tests.
    public static SuffixTree.Node ParseSource(IDocumentSource source) {
        DocumentReader reader = new DocumentReader(source);
        reader.Read();
        return reader.Tree().Root();
    }

    /**
     * Returns a list with all clusters from the document 
     * that meet conditions specified in the parameters.
     *
     * @param source The source from where to read the documents.
     * @param clusterOverlapDegree The minimum overlapping degree
     * for two clusters to be combined into a single one.
     * @param maxClusters The maximum number of clusters to add to the
     * result lists. The rest of the documents are added to a cluster named "Other".
     * @param minClusterWeight The minimum weight of a cluster to be considered.
     * @return A list with all clusters meeting the specified conditions.
     */
    public static List<Cluster> Find(IDocumentSource source,
                                     double clusterOverlapDegree,
                                     int maxClusters, double minClusterWeight) {
        assert(source != null);
        assert(maxClusters > 0);
        // ------------------------------------------------
        // Read all documents and get the base clusters.
        // The weight of each one is computed and is used to sort them
        // in ascending order. Clusters with low weight are grupped
        // under a single cluster named "Other", but only if they remain
        // after 'maxClusters' have been considered.
        DocumentReader reader = new DocumentReader(source);
        reader.Read();

        List<Cluster> baseClusters = reader.GetBaseClusters(minClusterWeight);

        if(baseClusters.isEmpty()) {
            return new ArrayList<Cluster>();
        }

        // Select the first 'maxClusters' clusters.
        Collections.sort(baseClusters);
        int limit = Math.min(maxClusters, baseClusters.size());

        List<Cluster> toMerge = baseClusters.subList(0, limit);
        ClusterMerger merger = new ClusterMerger(toMerge, clusterOverlapDegree);
        List<Cluster> finalClusters = merger.MergeClusters();

        if(limit < baseClusters.size()) {
            // Some clusters remained, group them under a single cluster.
            Cluster other = Cluster.Merge(baseClusters.subList(limit, baseClusters.size()));
            other.SetLabel("Other");
            finalClusters.add(other);
        }

        return finalClusters;
    }
}
