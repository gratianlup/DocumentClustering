package Clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReutersParser {
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SS", Locale.US);

	private static final String reuterSuffix1 = " Reuter\n&#3;";
	private static final String reuterSuffix2 = " REUTER\n&#3;";

	private static final String dataPrefix = "reut2-";
	private static final String startArticle = "<REUTERS";
	private static final String endArticle = "</REUTERS";

	private File data;

	public ReutersParser(File data){
		this.data = data;
	}

	public List<Article> parse()
	{
		return Arrays.stream(data.listFiles())
			.filter(f -> f.getName().startsWith(dataPrefix))
			.map(ReutersParser::toBufferedReader)
			.flatMap(this::splitFile)
			.map(this::parseArticle)
			.filter(a -> !a.topics().isEmpty())
			.filter(a -> !a.bodyWords().isEmpty())
			.collect(Collectors.toList());
	}

	private Stream<String> splitFile(BufferedReader br) {
		Stream.Builder<String> articleStrings = Stream.builder();
		for (String ln; (ln = lineFrom(br)) != null;)
		{
			if (!ln.startsWith(startArticle)) continue;

			StringBuilder sb = new StringBuilder();
			while (!ln.startsWith(endArticle)) {
				sb.append(ln).append("\n");
				ln = lineFrom(br);
			}
			articleStrings.add(sb.toString());
		}
		return articleStrings.build();
	}

	private Article parseArticle(String s) {
		String titleTag = parseTag("TITLE", s);
		String bodyTag = parseTag("BODY", s);
		String dateTag = parseTag("DATE", s);
		Set<String> topics = parseListTag("TOPICS", s);
		Set<String> places = parseListTag("PLACES", s);
		Set<String> people = parseListTag("PEOPLE", s);
		Set<String> orgs = parseListTag("ORGS", s);
		Set<String> exchanges = parseListTag("EXCHANGES", s);

		if (bodyTag.endsWith(reuterSuffix1) || bodyTag.endsWith(reuterSuffix2))
		{
			int last = bodyTag.length() - reuterSuffix1.length();
			bodyTag = bodyTag.substring(0, last);
		}

		return new Article(titleTag, bodyTag, topics);
	}

	private Set<String> parseListTag(String tag, String text) {
		return new HashSet<String>(
				Arrays.stream(
						parseTag(tag, text)
							.replace("</D>", " ")
							.replace("<D>", " ")
							.split("\\s+"))
					.filter(x -> !x.equals(""))
					.collect(Collectors.toList())
				);
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

	private static BufferedReader toBufferedReader(File f){
		try { return new BufferedReader(new FileReader(f)); }
		catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}

	private String lineFrom(BufferedReader br) {
		try { return br.readLine();	}
		catch (IOException e) { throw new RuntimeException(e); }
	}
}
