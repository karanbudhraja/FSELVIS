package experiment;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

public class SingleRunBackup {
	private static boolean	IS_VERBOSE	= false;
	private static double	START_GUESS	= 2.001;
	private static double	ACCURACY	= 0.999;
	private static int		NUM_FEAT	= 10;
	private static double	AVG_UTILITY	= 1.0;
	private static double 	THRESHOLD 	= 1.0;
	private static int 		NUM_ROUNDS 	= 25;
	private static int		NUM_GAMES	= 5;
	private static String	FILE_PATH	= "/users/denizen/Desktop/AfsResults/testing.txt";

	public static void main(String[] args) {
		System.out.println("Running SingleRunBackup");
		runExperiment();
	}
	
	public static void runExperiment() {
		Writer myWriter = new Writer(FILE_PATH);
		
		double advSum = 0;
		double selSum = 0;
		for(int j = 0; j < NUM_GAMES; j++) {

			//make utility function
			//Constant discountFunction = new Constant(0.5);
			Linear discountFunction = new Linear(-0.05, 1);
			Naive1D utilityFunction = new Naive1D(discountFunction, false);
			for(int i = 0; i < NUM_FEAT; i++) {
				utilityFunction.addFeature(i, Math.random()*2.0*AVG_UTILITY);
			}

			//make adversary (+ give features)
			AbsAdv adversary = new BinarySearch(START_GUESS, ACCURACY, utilityFunction);
			adversary.setVerbose(IS_VERBOSE);
			for(int i = 0; i < NUM_FEAT; i++) {
				adversary.addFeature(i);
			}

			//make selector
			AbsSel selector = new UtilityThreshold(THRESHOLD, utilityFunction);
			selector.setVerbose(IS_VERBOSE);

			//run random number of rounds
			for(int i = 0; i < NUM_ROUNDS; i++) {
				adversary.giveOffer(selector);
			}

			//output total utility 
	/*		System.out.println("Adversary: utility=" + (adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost()));
			System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			System.out.println("---------------"); 
	*/		
			advSum += adversary.getUtility(); 
			selSum += selector.getUtility()- selector.getCost();
		}
		myWriter.toBuffer((advSum/(double)NUM_GAMES) + ", " + (selSum/(double)NUM_GAMES) + "\r\n");
		myWriter.write();
	}

}
