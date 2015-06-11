package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

public class StanfordParserTest {
	public static void main (String[] args) {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// read some text in the text variable
		System.out.println ("[INFO] type exit to leave.");
		String text = "";
		while (true) {
			try {
				text = de.uni_leipzig.informatik.swp15_sc.utils.IOUtils.readln ("type something");
				if (text.equals("exit")) break;
			}
			catch (Exception e) {
				System.err.println (e);
				System.exit (-1);
			}

			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);
			
			// run all Annotators on this text
			pipeline.annotate(document);
			
			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
			List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

			System.out.println ("|--- word, pos-tag, named entity tag ---|");
			
			for(CoreMap sentence: sentences) {
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
					// this is the text of the token
					String word = token.get(CoreAnnotations.TextAnnotation.class);
					// this is the POS tag of the token
					String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
					// this is the NER label of the token
					String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
					
					System.out.println (word + ", " + pos + ", " + ne);
				}

				// this is the parse tree of the current sentence
				Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
				System.out.println(tree);

				// this is the Stanford dependency graph of the current sentence
				SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
				System.out.println(dependencies);
			}

			// This is the coreference link graph
			// Each chain stores a set of mentions that link to each other,
			// along with a method for getting the most representative mention
			// Both sentence and token offsets start at 1!
			//Map<Integer, CorefChain> graph = 
			//	document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
		}
	}
}
