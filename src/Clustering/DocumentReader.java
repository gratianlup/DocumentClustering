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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public final class DocumentReader {
    // Fiecare fraza trebuie sa se termine intr-un cuvant unic pentru a fi
    // corect introdusa in arborele generalizat. Ex: $0, $1, ... $100, ...
    private static final String END_MARKER = "#";

    /*
    * Membrii.
    */
    private IDocumentSource source_;
    private LinkedHashMap<String, Word> words_; // Contine toate cuvintele gasite.
    private HashMap<Word, Integer> wordDf_;     // In cate documente a aparut un cuvant.
    private ArrayList<Document> documents_;
    private int phraseCount_;
    private SuffixTree tree_;

    /*
     * Constructori.
     */
    public DocumentReader(IDocumentSource source) {
        source_ = source;
        words_ = new LinkedHashMap<String, Word>();
        documents_ = new ArrayList<Document>();
        wordDf_ = new HashMap<Word, Integer>();
        tree_ = new SuffixTree();
    }

    /*
     * Metode publice.
     */
    // Citeste toate documentele care pot fi obtinute de la sursa data.
    public void Read() {
        while(source_.HasDocument()) {
            ReadDocument(source_);
        }

        // Calculeaza importanta cuvintelor.
        ComputeWeights();
    }

    public List<Cluster> GetBaseClusters(double minWeight) {
        return tree_.GetBaseClusters(minWeight);
    }

    public List<Document> Documents() { return documents_; }
    public SuffixTree Tree() { return tree_; }

    /*
     * Metode private.
     */
    // Citeste toate propozitiile dintr-un document si actualizeaza statisticile.
    private Document ReadDocument(IDocumentSource source) {
        Document doc = new Document(documents_.size());

        while(source.HasSentence()) {
            ReadSentence(doc, source);
        }

        documents_.add(doc);
        return doc;
    }

    // Citeste o propozitie din documentul dat.
    private void ReadSentence(Document doc, IDocumentSource source) {
        int startIndex = doc.Count(); // Nr. de cuvinte inainte de a citi propozitia.
        int endIndex;

        while(source.HasWord()) {
            // Obtine cuvantul, apoi actualizeaza documentul si numarul de aparitii.
            String wordStr = source.NextWord();
            Word word = words_.get(wordStr);
            if(word != null) {
                // Cuvantul a mai fost intalnit (posibil in alte documente).
                if(doc.ContainsWord(word) == false) {
                    // Prima aparitie a cuvantului in documentul curent.
                    int newCount = wordDf_.get(word) + 1;
                    wordDf_.put(word, newCount);
                }
            }
            else {
                // Prima data cand se intalneste cuvantul.
                word = new Word(wordStr);
                words_.put(wordStr, word);
                wordDf_.put(word, 1);
            }

            doc.AddWord(word);
        }

        // Adauga un marcator de sfarsit al frazei (necesar).
        String marker = END_MARKER + Integer.toString(phraseCount_++);
        Word markerWord = new Word(marker);
        words_.put(marker, markerWord);
        wordDf_.put(markerWord, 1);
        doc.AddWord(markerWord);

        // Introduce propozitia citita in arborele de sufixe.
        endIndex = doc.Count();
        tree_.AddSentence(doc, startIndex, endIndex);
    }

    // Calculeaza media a 'term frequency' din toate documentele.
    private double AverageTf(Word word) {
        double sum = 0;
        
        int count = documents_.size();
        for(int i = 0; i < count; i++) {
            sum += documents_.get(i).TermFrequency(word);
        }

        return sum / (double)wordDf_.get(word); // Garantat diferit de 0.
    }

    // Calculeaza importanta fiecarui cuvint citit.
    // Importanta este data de produsul dintre numarul de aparitii ale cuvantului
    // in documentul de care apartine (tf) inmultit cu inversul
    // numarului de documente in care apare cuvantul (idf).
    // Se presupune ca 'tf' este deja setat in campul 'Weight' al cuvintelor,
    // iar 'df' trebuie sa se gaseasca in tabela 'wordDf'.
    private void ComputeWeights() {
        int docs = documents_.size();
        
        if(docs == 0) {
            return;
        }
        
        Iterator<Word> wordIt = words_.values().iterator();
        while(wordIt.hasNext()) {
            Word word = wordIt.next();
            double df = (double)wordDf_.get(word);

            // Calculeaza importanta ('df' este garantat diferit de 0).
            double weight = (1.0 + Math.log10(AverageTf(word))) *
                            Math.log10(1.0 + ((double)docs / df));
            word.SetWeight(weight);
        }
    }
}