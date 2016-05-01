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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public final class DocumentReader {
	// Each sentence must end in an unique word
	// to be inserted properliy in the suffix tree.
	// For example, $0, $1, ... $100, ...
	private static final String END_MARKER = "#";

	/*
	 * Private members.
	 */
	private IDocumentSource source_;
	private LinkedHashMap<String, Word> words_; // Contains all the found words.
	private HashMap<Word, Integer> wordDf_; // The number of documents in which
											// a word has been found.
	private ArrayList<Document> documents_;
	private int phraseCount_;
	private SuffixTree tree_;

	/*
	 * Constructors.
	 */
	public DocumentReader(IDocumentSource source) {
		source_ = source;
		words_ = new LinkedHashMap<String, Word>();
		documents_ = new ArrayList<Document>();
		wordDf_ = new HashMap<Word, Integer>();
		tree_ = new SuffixTree();
	}

	/*
	 * Public methods.
	 */
	// Reads all documents from the specified source.
	public void Read() {
		while (source_.HasDocument()) {
			ReadDocument(source_);
		}

		ComputeWeights();
	}

	public Set<Cluster> GetBaseClusters(double minWeight) {
		return tree_.GetBaseClusters(minWeight);
	}

	public List<Document> Documents() {
		return documents_;
	}

	public SuffixTree Tree() {
		return tree_;
	}

	/*
	 * Private methods.
	 */
	// Reads all sentences from a document and updates the statistics.
	private Document ReadDocument(IDocumentSource source) {
		Document doc = new Document(documents_.size());

		while (source.HasSentence()) {
			ReadSentence(doc, source);
		}

		documents_.add(doc);
		return doc;
	}

	// Reads and parses a sentence from the specified document.
	private void ReadSentence(Document doc, IDocumentSource source) {
		int startIndex = doc.Count(); // The number of words before the
										// sentence.
		int endIndex;

		while (source.HasWord()) {
			// Obtain the word, then update the document and the statistics.
			String wordStr = source.NextWord();
			Word word = words_.get(wordStr);

			if (word != null) {
				// The word has been found before (possible in other documents
				// too).
				if (!doc.ContainsWord(word)) {
					// This is the first time the word has been found
					// in the current docuemtn, add an entry for it.
					int newCount = wordDf_.get(word) + 1;
					wordDf_.put(word, newCount);
				}
			} else {
				// The first time when the word is found
				// in any docuemtn, add an entry for it.
				word = new Word(wordStr);
				words_.put(wordStr, word);
				wordDf_.put(word, 1);
			}

			doc.AddWord(word);
		}

		// Add a sentence end marker (required by the suffix tree).
		String marker = END_MARKER + Integer.toString(phraseCount_++);
		Word markerWord = new Word(marker);
		words_.put(marker, markerWord);
		wordDf_.put(markerWord, 1);
		doc.AddWord(markerWord);

		// Add the read sentence to the suffix tree.
		endIndex = doc.Count();
		tree_.AddSentence(doc, startIndex, endIndex);
	}

	// Computes the term frequence average for the specified word
	// for all documents in which it is found.
	private double AverageTf(Word word) {
		double sum = 0;
		int count = documents_.size();

		for (int i = 0; i < count; i++) {
			sum += documents_.get(i).TermFrequency(word);
		}

		// It is guaranteed that the word appears at least once.
		return sum / (double) wordDf_.get(word);
	}

	// Computes the weight of each read word.
	private void ComputeWeights() {
		// The importante is equal to the product between the number of times
		// the word appears in the document (term frequence) with
		// the inverted document frequence. It is presumed that the term
		// frequence
		// has been already computed and is availalbe in the 'Weight' field
		// of each word, and the term frequence must be found in the 'wordDf'
		// map.
		Iterator<Word> wordIt = words_.values().iterator();
		int docs = documents_.size();

		if (docs == 0) {
			return;
		}

		while (wordIt.hasNext()) {
			Word word = wordIt.next();
			double df = (double) wordDf_.get(word);

			double tf = AverageTf(word);
			double idf = Math.log10(((double) docs / df));
			double weight = tf * idf;
			word.SetWeight(weight);
		}
	}
}
