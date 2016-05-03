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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public final class Cluster implements Comparable<Cluster> {
	private ArrayList<Document> documents_;
	private List<Phrase> phrases_;
	private double weight_;
	private String label_;

	/*
	 * Constructors.
	 */
	public Cluster(int docCapacity, int phraseCapacity) {
		documents_ = new ArrayList<Document>(docCapacity);
		phrases_ = new ArrayList<Phrase>(phraseCapacity);
	}

	public Cluster(Phrase phrase) {
		this(4, 1);
		phrases_.add(phrase);
	}

	/*
	 * Public methods.
	 */
	// Computes the weight of the clusters based on the contained documents.
	public void ComputeWeight() {
		// The weight is equal to the product between the number of documents,
		// the (adjusted) length of the sentences and the sum of the weight
		// of each words part of the sentences.
		double wordWeight = 0;
		int count = phrases_.size();

		for (int i = 0; i < count; i++) {
			wordWeight += phrases_.get(i).Weight();
		}

		weight_ = documents_.size() * PhrasesWeight() * wordWeight;
	}

	/*
	 * Returns the 'distance' between this cluster and another. The distance can
	 * be thought of as the average similarity of the documents in the two clusters.
	 * i.e. a measure of how overlapping the clusters are. If the two clusters are exactly
	 * identical then the similarity would be 1. If there is no overlap then the distance would be 0.
	 */
	public double similarity(Cluster other) {
		Hashtable<Document, Document> hash = new Hashtable<Document, Document>();

		for (int i = 0; i < documents_.size(); i++) {
			Document doc = documents_.get(i);
			hash.put(doc, doc);
		}

		// Check which of the documents from the other clusters
		// are found in the hash table.
		double common = 0;
		for (int i = 0; i < other.documents_.size(); i++) {
			if (hash.containsKey(other.documents_.get(i))) {
				common++;
			}
		}

		double dist_forward = common / (double) documents_.size();
		double dist_backward = common / (double) other.documents_.size();

		// Return the average distance between these two clusters.
		return (dist_forward + dist_backward) / 2.0 ;
	}

	// Unifies all clusters from the specified list
	// into a single cluster containing the union of the documents.
	public static Cluster Merge(Set<Cluster> clusters) {
		assert(clusters != null);

		Cluster newCluster = new Cluster(clusters.size() * 2, clusters.size());
		Set<Document> allDocuments = new HashSet<>();

		// Each document must appear a single time in the new cluster, as must
		// each Phrase in each original cluster.
		for (Cluster c : clusters) {
			allDocuments.addAll(c.Documents());
			newCluster.Phrases().addAll(c.Phrases());
		}

		// Add the documents to the new cluster.
		newCluster.Documents().addAll(allDocuments);
		return newCluster;
	}

	public static Cluster Merge(List<Cluster> clusters) {
		Set<Cluster> clusterSet = new HashSet<>();
		clusterSet.addAll(clusters);
		return Merge(clusterSet);
	}

	public double Weight() {
		return weight_;
	}

	public List<Document> Documents() {
		return documents_;
	}

	public List<Phrase> Phrases() {
		return phrases_;
	}

	public void SetPhrases(List<Phrase> value) {
		phrases_ = value;
	}

	public String Label() {
		return label_;
	}

	public void SetLabel(String value) {
		label_ = value;
	}

	/*
	 * Private methods.
	 */
	private double PhrasesWeight() {
		double sum = 0;
		int count = phrases_.size();

		for (int i = 0; i < count; i++) {
			sum += phrases_.get(i).WordCount();
		}

		if (sum < 2) {
			return 0.5;
		} else {
			return Math.min(6, sum);
		}
	}

	public int compareTo(Cluster other) {
		// Used when sorting clusters based on their weight.
		if (other == this) {
			return 0;
		}

		// Clusters with a lower weight come first.
		return ((int) other.weight_ - (int) weight_);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster: ");
		sb.append(label_);
		sb.append("; Weight: ");
		sb.append(weight_);
		sb.append("; Number docs: ");
		sb.append(documents_.size());
		for (Phrase p : phrases_) {
			sb.append(p.toString());
		}
		return sb.toString();
	}
	
	public List<Article> articles(List<Article> articles) {
		List<Article> retArticles = new ArrayList<>();
		for (Document d : documents_) {
			retArticles.add(articles.get(d.Index()));
		}
		return retArticles;
	}

	public void printArticles(List<Article> articles) {
		for (Document d : documents_) {
			System.out.println("--------------------------------------");
			System.out.println(articles.get(d.Index()).bodyTag());
		}
	}
}
