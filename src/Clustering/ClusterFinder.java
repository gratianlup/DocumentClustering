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
    // Pentru teste.
    public static SuffixTree.Node ParseSource(IDocumentSource source) {
        DocumentReader reader = new DocumentReader(source);
        reader.Read();
        return reader.Tree().Root();
    }

    /**
     * Returneaza o lista cu clusterele din document care indeplinesc
     * conditiile specificate in parametrii.
     * @param source Sursa de unde trebuie citite documentele
     * @param clusterOverlapDegree Coeficientul de asemanare necesar pentru ca
     * doua clustere sa fie combinate
     * @param maxClusters Numarul maxim de clustere de returnat. Clusterele care
     * raman vor fi introduse intr-un cluster denumit "Other"
     * @param minClusterWeight Importanta minima a unui cluster pentru a fi luat
     * in considerare
     * @return Lista cu clusterele gasite.
     */
    public static List<Cluster> Find(IDocumentSource source,
                                     double clusterOverlapDegree,
                                     int maxClusters, double minClusterWeight) {
        assert(source != null);
        assert(maxClusters > 0);
        // ------------------------------------------------
        // Citeste toate documentele si obtine cluster-ele de baza.
        // Importanta acestora va fi determinata, apoi vor fi sortate in
        // functie de importanta. Cluster-ele cu importanta scazuta (sau care
        // raman dupa ce s-au ales 'maxClusters' clustere) vor fi grupate
        // intr-un cluster denumit 'Other'.
        DocumentReader reader = new DocumentReader(source);
        reader.Read();

        List<Cluster> baseClusters = reader.GetBaseClusters(minClusterWeight);

        if(baseClusters.isEmpty()) {
            return new ArrayList<Cluster>();
        }

        // Alege primele 'maxClusters' si grupeaza-le.
        // Restul vor fi unite sub numele 'Other'.
        Collections.sort(baseClusters);
        int limit = Math.min(maxClusters, baseClusters.size());

        List<Cluster> toMerge = baseClusters.subList(0, limit);
        ClusterMerger merger = new ClusterMerger(toMerge, clusterOverlapDegree);
        List<Cluster> finalClusters = merger.MergeClusters();

        if(limit < baseClusters.size()) {
            // Au mai ramas clustere.
            Cluster other = Cluster.Merge(baseClusters.subList(limit, baseClusters.size()));
            other.SetLabel("Other");
            finalClusters.add(other);
        }

        return finalClusters;
    }
}