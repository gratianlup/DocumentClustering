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
import java.util.List;

public final class Phrase {
    private List<Word> words_;

    /*
    * Constructors.
    */
    public Phrase() { words_ = new ArrayList<Word>(); }

    /*
    * Public methods.
    */
    public List<Word> Words() { return words_; }
    public int WordCount() { return words_.size(); }

    public double Weight() {
        double sum = 0;
        int count = words_.size();
        
        for(int i = 0; i < count; i++) {
            sum += words_.get(i).Weight();
        }

        return sum;
    }

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();

        for(Word word : words_) {
            sb.append(word.GetWord());
            sb.append(" ");
        }

        return sb.toString();
    }
}
