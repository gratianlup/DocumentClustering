package Clustering;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Article {

	private Set<String> topics;
	private String titleTag;
	private String bodyTag;

	public Article(String titleTag, String bodyTag, Set<String> topics) {
		this.titleTag = titleTag;
		this.bodyTag = bodyTag;
		this.topics = topics;
	}

	public List<String> bodyWords() {
		String[] bodyWords = bodyTag.split("\\s");
		List<String> bodyWordsList = new ArrayList<>();
		for (String s : bodyWords) {
			bodyWordsList.add(s);
		}
		return bodyWordsList;
	}

	public Queue<Queue<String>> sentences() {
		Queue<Queue<String>> sentences = new ArrayDeque<>();

		for (String s : bodyTag.split("\\.")) {
			Queue<String> sentence = new ArrayDeque<>();
			for (String word : s.split("\\s")) {
				// Skip blank lines.
				if (word.length() == 0) {
					continue;
				}
				sentence.offer(word);
			}
			// Add the queue of words representing one sentence into the
			// queue of sentences.
			sentences.offer(sentence);
		}

		return sentences;
	}
	
	public Set<String> topics() {
		return this.topics;
	}
	
	public String titleTag() {
		return this.titleTag;
	}
	
	public String bodyTag() {
		return this.bodyTag;
	}

}