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

import java.util.HashSet;
import java.util.Set;

/**
 * Merges base clusters to produce final clusters using a minimum overlap
 * threshold.
 */
public class MinDegreeClusterMerger extends AbstractOverlappingClusterMerger {

	public MinDegreeClusterMerger(double minOverlapDegree) {
		this.minOverlapDegree = minOverlapDegree;
	}

	public Set<Cluster> MergeClusters(Set<Cluster> baseClustersToMerge) {
		// Build a graph of similar base clusters.
		Set<GraphVertex> vertices = generateVertices(baseClustersToMerge);
		ClusterGraph cg = ClusterGraph.buildGraph(vertices, minOverlapDegree);

		// Find the different connected components within the graph produced above.
		Set<ClusterGraph> connectedComponents = cg.getConnectedComponents();

		// Unify the clusters from each connected component
		// into a single one and add them to the resulting list.
		Set<Cluster> clusters = new HashSet<>();
		for (ClusterGraph connectedComponent : connectedComponents) {
			Set<Cluster> clustersInConnectedComponent = connectedComponent.getClusters();
			clusters.add(Cluster.Merge(clustersInConnectedComponent));
		}

		return clusters;
	}
}
