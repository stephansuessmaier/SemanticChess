package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import java.util.*;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

/**
 * This class is for tagging a natural language question by using the Stanford
 * parser
 * 
 * 
 * @author Stephan Suessmaier
 *
 */
public class QuestionTagger {
	/**
	 * Creates a new QuestionTagger, this may take some time.
	 */
	public QuestionTagger() {
		props = new Properties();
//		props.setProperty("annotators",
//				"tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * Tags a question
	 * @param question to tag
	 * @return array of tagged words
	 */
	public TaggedWord[] tagQuestion(String question) {
		LinkedList<TaggedWord> twords = new LinkedList<TaggedWord>();
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(question);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document
				.get(CoreAnnotations.SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence
					.get(CoreAnnotations.TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token
						.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token
						.get(CoreAnnotations.NamedEntityTagAnnotation.class);
				//checks if multiple words in a row are tagged as NN and merges the TaggedWord entries 
				if(pos.contains("NN") && !twords.isEmpty()){
					if(twords.getLast().getPos().equals(pos) && twords.getLast().getNe().equals("O") && ne.equals("O")){
						word = twords.getLast().getWord() + " " + word;
						twords.removeLast();
					}
					else if(ne.equals(twords.getLast().getNe()) && ne.equals("O") == false){
						word = twords.getLast().getWord() + " " + word;
						//checks if pos-tags differ, merges them if this is the case
						if(pos.equals(twords.getLast().getPos()) == false){
							pos = twords.getLast().getPos() + "," + pos;
						}
						twords.removeLast();
					}
				}
				//checks if a word is in ECO format, changes NE tag if so
				//first character needs to be a-e, second and third char need to be numbers
				if(word.length() == 3 && Pattern.matches("[a-eA-E]", word.substring(0,1)) && Pattern.matches("[0-9]+", word.substring(1))){
					ne = "ECO";
				}
				
					
				twords.addLast(new TaggedWord(word, pos, ne));
			}
		}
		
		// convert list into array
		TaggedWord[] result = new TaggedWord[twords.size()];
		twords.toArray(result);
		return result;
	}

	private Properties props;
	private StanfordCoreNLP pipeline;
}
