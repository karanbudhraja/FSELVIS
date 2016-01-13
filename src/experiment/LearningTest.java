package experiment;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

public class LearningTest {
	private static double	BASE_Q			= 3;
	private static double	EPSILON			= 0.0;
	private static double	LEARNING_RATE	= 1;
	private static double	DISCOUNT_FACTOR	= 1;
	
	private static boolean	IS_VERBOSE		= false;
	private static int		NUM_GAMES		= 2000;
	//optimal: 2->1->3
	private static int	 	NUM_ROUNDS		= 30;
	private static double 	THRESHOLD 		= 50;
	private static int		NUM_FEAT		= 8; 
	private static double	DISCOUNT		= 0.5;
	private static double	START_GUESS		= 75.001;
	private static double	ACCURACY		= 0.95;

	
	private static String IN_PATH = "/users/denizen/Desktop/AfsResults/input/letter";
	private static String	OUT_PATH	= "/users/denizen/Desktop/AfsResults/learningTest.txt";
	
	private static Writer myWriter;
	private static int counter;

	public static void main(String[] args) {
		counter = 0;
		
		System.out.println("Running LearningTest");
		myWriter = new Writer(OUT_PATH);
		myWriter.toBuffer("numRounds, threshold, numFeat, avgUtility, startGuess, accuracy, advUtility, selUtility\r\n");
		myWriter.write();
		
		runExperiment(NUM_GAMES, NUM_ROUNDS, THRESHOLD, NUM_FEAT, DISCOUNT, START_GUESS, ACCURACY);
		
		System.out.println("Begin write");
		myWriter.write();
		System.out.println("Done!");
	}
	
	public static void runExperiment(int numGames, int numRounds, 
			double threshold, int numFeat, double discount, double startGuess, double accuracy) {
		System.out.println(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				discount + ", " + 
				startGuess + ", " + 
				accuracy);
		System.out.println(++counter);
				
		double advSum = 0;
		double selSum = 0;
		
		//make utility function
		Constant discountFunction = new Constant(discount);
		//Linear discountFunction = new Linear(-0.05, 1);
		
		NDimen utilityFunction = new NDimen(discountFunction, IN_PATH, 1);
		
		//make adversary (+ give features)
		AbsAdv adversary = new LearningWithBinarySearch(startGuess, accuracy, utilityFunction, 
				BASE_Q, EPSILON, LEARNING_RATE, DISCOUNT_FACTOR);
		
		for(int j = 0; j < numGames; j++) {
			adversary.setVerbose(IS_VERBOSE);
			if(IS_VERBOSE) System.out.println("Game #" + (j+1));
			((LearningWithBinarySearch)adversary).resetForLearning(startGuess);
			for(int i = 1; i <= numFeat; i++) {					//TODO: Changed i=0; i< to i=1; i<= ; Should work properly (need to double-check)
				adversary.addFeature(i);
			}
			

			//make selector
			AbsSel selector = new UtilityThreshold(threshold, utilityFunction);
			selector.setVerbose(IS_VERBOSE);

			//run random number of rounds
			for(int i = 0; i < numRounds; i++) {
				adversary.giveOffer(selector);
			}

			//output total utility 
			System.out.println("Adversary: utility=" + (adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost()));
			//System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			//System.out.println("---------------"); 
			//((LearningWithBinarySearch)adversary).dumpPolicy();

			advSum += adversary.getUtility()/* + adversary.evaluateUtility() - adversary.getCost()*/; 
			selSum += selector.getUtility() + selector.evaluateUtility() - selector.getCost();
		}
		myWriter.toBuffer(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				discount + ", " + 
				startGuess + ", " + 
				accuracy + ", " + 
				(advSum/(double)numGames) + ", " + 
				(selSum/(double)numGames) + "\r\n");
	}

}
