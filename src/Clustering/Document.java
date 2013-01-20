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
import java.util.Iterator;
import java.util.LinkedHashMap;

public final class Document {
    private ArrayList<Word> words_;
    private LinkedHashMap<Word, Integer> wordCount_;
    private int index_; // Numarul documentului in sursa.

    /*
     * Constructori.
     */
    public Document(int index) {
        index_ = index;
        words_ = new ArrayList<Word>();
        wordCount_ = new LinkedHashMap<Word, Integer>();
    }

    /*
     * Metode publice.
     */
    public void AddWord(Word word) {
        assert(word != null);
        // ------------------------------------------------
        words_.add(word);
        Integer count = wordCount_.get(word);
        if(count == null) {
            wordCount_.put(word, 1);
        }
        else {
            wordCount_.put(word, count + 1);
        }
    }

    public Word WordAt(int index) {
        assert(index >= 0 && index < words_.size());
        // ------------------------------------------------
        return words_.get(index);
    }

    // Numarul de aparitii ale cuvantului in document.
    public int WordCount(Word word) {
        assert(word != null);
        // ------------------------------------------------
        Integer count = wordCount_.get(word);
        
        if(count == null) {
            return 0;
        }
        else {
            return count;
        }
    }

    // Raportul dintre numarul de aparitii ale unui cuvant si
    // numarul total de cuvinte din document.
    public double TermFrequency(Word word) {
        return (double)WordCount(word) / (double)words_.size();
    }

    public int Count() { return words_.size(); }

    public int Index() { return index_; }

    public boolean ContainsWord(Word word) {
        assert(word != null);
        // ------------------------------------------------
        return wordCount_.containsKey(word);
    }

    public void Clear() {
        words_.clear();
        wordCount_.clear();
    }

    public Iterator<Word> Iterator() {
        return words_.iterator();
    }

    @Override
    public String toString() { // Pentru debugging.
        return "Words: " + Integer.toString(words_.size());
    }
}