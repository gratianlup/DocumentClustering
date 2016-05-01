package Clustering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Document source suitable for representing the Reuters document corpus (or any
 * corpus split across multiple files)
 *
 * @author Harry Ross - harryross263@gmail.com
 */
public class ReutersSource implements IDocumentSource {
	/* The list of files making up the Reuters corpus. */
	private Queue<File> files;

	/* A collection of the sentences that make up the current document */
	private Queue<Queue<String>> sentences;

	/* A collection of the words that make up the current sentence */
	private Queue<String> currSentence;

	/* The topics included in the current document. */
	private Set<String> currTopics;

	public ReutersSource(File folder) {
		this.files = readFiles(folder);
	}

	public Queue<File> readFiles(File folder) {
		Queue<File> newFiles = new ArrayDeque<>();
		for (File file : folder.listFiles()) {
			newFiles.add(file);
		}
		return newFiles;
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
		if (files == null || files.isEmpty()) {
			sentences.clear();
			System.out.println("Read all documents from this source");
			return false;
		}

		sentences = new ArrayDeque<>();
		currTopics = new HashSet<>();
		try(Stream<String> stream = Files.lines(files.poll().toPath())) {
			stream.forEach(line -> {
				if (line.startsWith("<D>")) {
					// Read into current topics.
					readTopics(line);
					return;
				}
				Queue<String> sentence = new ArrayDeque<>();
				for (String word : line.split("\\s")) {
					// Skip blank lines.
					if (word.length() == 0)
						continue;
					sentence.offer(word);
				}
				// Add the queue of words representing one sentence into the
				// queue of sentences.
				sentences.offer(sentence);
			});

			if (currTopics.isEmpty()) {
				// This document has no topics, so read the next one.
				return readDocument();
			}
			return true;
		} catch (IOException e) {
			System.out.println(e);
			throw new RuntimeException("File not found");
		}
	}

	public void readTopics(String line) {
		currTopics = new HashSet<String>(
				Arrays.stream(
						parseTag("TOPICS", line)
							.replace("</D>", " ")
							.replace("<D>", " ")
							.split("\\s+"))
					.filter(x -> !x.equals(""))
					.collect(Collectors.toList())
				);
		for (String s : currTopics) {
			System.out.println(s);
		}
	}

	private String parseTag(String tag, String text) {
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";
		int startTagIndex = text.indexOf(startTag);
		if (startTagIndex < 0) return "";
		int start = startTagIndex + startTag.length();
		int end = text.indexOf(endTag, start);
		if (end < 0) throw new IllegalArgumentException("no end, tag=" + tag + " text=" + text);
		return text.substring(start, end);
	}

	@Override
	public boolean HasDocument() {
		if (HasSentence()) {
			// The STC implementation should only call this method once all of
			// the sentences
			// from the current document have been read.
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
			// Either sentences hasn't been initialised or we've read the entire
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
