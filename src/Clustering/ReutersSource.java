package Clustering;

import java.io.File;
import java.util.List;
import java.util.Queue;

/**
 * Document source suitable for representing the Reuters document corpus (or any
 * corpus split across multiple files)
 *
 * @author Harry Ross - harryross263@gmail.com
 */
public class ReutersSource implements IDocumentSource {

	private List<Article> articles;
	private int currentArticle;
	private final int n_documents;

	/* A collection of the sentences that make up the current document */
	private Queue<Queue<String>> sentences;

	/* A collection of the words that make up the current sentence */
	private Queue<String> currSentence;

	public ReutersSource(File folder, int n_documents) {
		this.articles = new ReutersParser(folder).parse();
		this.n_documents = n_documents;
		currentArticle = 0;
	}

	/**
	 * Constructs a queue of sentences that represent the next document. Each
	 * sentence itself is a queue of words such that polling until empty will
	 * re-construct the original sentence.
	 *
	 * Should be called once for each document in the source.
	 *
	 * Returns whether a document was successfully read or not.
	 */
	public boolean readDocument() {
		if (currentArticle >= n_documents) {
			System.out.println("Terminating at " + n_documents + " articles for testing...");
			return false;
		}
		
		if (articles == null || articles.isEmpty() || currentArticle >= articles.size() - 1) {
			sentences.clear();
			System.out.println("Read all documents from this source");
			return false;
		}
		
		currentArticle++;
		sentences = articles.get(currentArticle).sentences();
		return true;
	}

	public List<Article> articles() {
		return this.articles;
	}

	@Override
	public boolean HasDocument() {
		if (HasSentence()) {
			// The STC implementation should only call this method once all of
			// the sentences from the current document have been read.
			throw new RuntimeException("HasDocument called before the current document has been completely read.");
		}
		// The current document has been fully read, so attempt to read in the
		// next document in the queue.
		return readDocument();
	}

	/**
	 * Indicates whether the current document has another sentence or not.
	 *
	 * @return
	 */
	@Override
	public boolean HasSentence() {
		if (HasWord()) {
			// The STC implementation should only call this method when all of
			// of the words in the current sentence have been read.
			throw new RuntimeException("HasSentence called before the current sentence has been completely read.");
		}

		return readSentence();
	}

	public boolean readSentence() {
		if (sentences == null || sentences.isEmpty()) {
			// Either sentences hasn't been initialized or we've read the entire
			// document.
			return false;
		}

		currSentence = sentences.poll();
		return true;
	}

	@Override
	public boolean HasWord() {
		if (currSentence == null) {
			return false;
		}

		return !currSentence.isEmpty();
	}

	@Override
	public String NextWord() {
		return currSentence.poll();
	}

}
