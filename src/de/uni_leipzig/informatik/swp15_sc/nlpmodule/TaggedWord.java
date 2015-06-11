package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

/**
 * This class represents a word with tags created by the Stanford parser
 * 
 * @author Stephan Suessmaier
 *
 */
public class TaggedWord {
	/**
	 * Creates a new TaggedWord
	 * 
	 * @param word
	 *            word
	 * @param pos
	 *            part of speech tag
	 * @param ne
	 *            named entity tag
	 */
	public TaggedWord(String word, String pos, String ne) {
		this.word = word;
		this.posTag = pos;
		this.namedEntityTag = ne;
	}

	/**
	 * Gets the word
	 * @return word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * Gets the part of speech tag
	 * @return pos tag
	 */
	public String getPos() {
		return posTag;
	}

	/**
	 * Gets the named entity tag
	 * @return named entity tag
	 */
	public String getNe() {
		return namedEntityTag;
	}

	public String toString() {
		return word + ", " + posTag + ", " + namedEntityTag;
	}

	private final String word;
	private final String posTag;
	private final String namedEntityTag;
}
