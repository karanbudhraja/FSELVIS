package experiment;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

public class FullCycle {
	private static boolean	IS_VERBOSE		= false;
	private static int		NUM_GAMES		= 100;
	
	private static double 	NUM_ROUNDS_L	= 10;
	private static double 	NUM_ROUNDS_H	= 50;
	
	private static double 	THRESHOLD_L 	= 0.5;
	private static double 	THRESHOLD_H 	= 2.0;
	
	private static double	NUM_FEAT_L		= 8; 					//should be 10
	private static double	NUM_FEAT_H		= 17; 			
	
	private static double	AVG_UTILITY_L	= 0.5;
	private static double	AVG_UTILITY_H	= 2.0;
	
	private static double	START_GUESS_L	= 2.001;
	private static double	START_GUESS_H	= 4.001;
	
	private static double	ACCURACY_L		= 0.5;
	private static double	ACCURACY_H		= 0.99;
	
	private static int 		INCREMENTS		= 5;
	
	private static String IN_PATH = "/users/denizen/Desktop/AfsResults/input/letter";
	private static String	OUT_PATH	= "/users/denizen/Desktop/AfsResults/fullCycle.txt";
	
	private static Writer myWriter;
	private static int counter;

	public static void main(String[] args) {
		counter = 0;
		
		System.out.println("Running FullCycle");
		myWriter = new Writer(OUT_PATH);
		myWriter.toBuffer("numRounds, threshold, numFeat, avgUtility, startGuess, accuracy, advUtility, selUtility\r\n");
		myWriter.write();
		
		double d = 0; //commented out avg_utility change since it is not being modified
		/*
		for(double a = NUM_ROUNDS_L; a <= NUM_ROUNDS_H; a += (NUM_ROUNDS_H-NUM_ROUNDS_L)/(double)(INCREMENTS-1))
			for(double b = THRESHOLD_L; b <= THRESHOLD_H; b += (THRESHOLD_H-THRESHOLD_L)/(double)(INCREMENTS-1))
				for(double c = NUM_FEAT_L; c <= NUM_FEAT_H; c += (NUM_FEAT_H-NUM_FEAT_L)/(double)(INCREMENTS-1))
					//for(double d = AVG_UTILITY_L; d <= AVG_UTILITY_H; d += (AVG_UTILITY_H-AVG_UTILITY_L)/(double)(INCREMENTS-1))
						for(double e = START_GUESS_L; e <= START_GUESS_H; e += (START_GUESS_H-START_GUESS_L)/(double)(INCREMENTS-1))
							for(double f = ACCURACY_L; f <= ACCURACY_H; f += (ACCURACY_H-ACCURACY_L)/(double)(INCREMENTS-1))
								runExperiment(NUM_GAMES, (int)a, b, (int)c, d, e, f);
		*/
		
		//Changed loop structure (increment counter and multiply by double rather than increment double)
		
		
		for(int a = 0; a < INCREMENTS; a++)
			for(int b = 0; b < INCREMENTS; b++)
				for(int c = 0; c < INCREMENTS; c++)
					//for(int d = 0; d < INCREMENTS; d++)
						for(int e = 0; e < INCREMENTS; e++)
							for(int f = 0; f < INCREMENTS; f++)
								runExperiment(NUM_GAMES, 
										(int)(	a*(NUM_ROUNDS_H		-NUM_ROUNDS_L)	/INCREMENTS 	+ NUM_ROUNDS_L),
												b*(THRESHOLD_H			-THRESHOLD_L) 		/INCREMENTS	+ THRESHOLD_L, 
										(int)(	c*(NUM_FEAT_H			-NUM_FEAT_L) 			/INCREMENTS	+ NUM_FEAT_L),
												d*(AVG_UTILITY_H		-AVG_UTILITY_L) 		/INCREMENTS	+ AVG_UTILITY_L,
												e*(START_GUESS_H		-START_GUESS_L) 	/INCREMENTS	+ START_GUESS_L,
												f*(ACCURACY_H				-ACCURACY_L) 			/INCREMENTS	+ ACCURACY_L
										);
		
		
		System.out.println("Begin write");
		myWriter.write();
		System.out.println("Done!");
	}
	
	public static void runExperiment(int numGames, int numRounds, 
			double threshold, int numFeat, double avgUtility, double startGuess, double accuracy) {
		/*System.out.println(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				avgUtility + ", " + 
				startGuess + ", " + 
				accuracy);*/
		System.out.println(++counter);
				
		double advSum = 0;
		double selSum = 0;
		
		//make utility function
		Constant discountFunction = new Constant(0.5);
		//Linear discountFunction = new Linear(-0.05, 1);
		
		NDimen utilityFunction = new NDimen(discountFunction, IN_PATH, 2);
		//utilityFunction.dump();
		/*Naive1D utilityFunction = new Naive1D(discountFunction);					//commented out for testing NDimen
		
		
		for(int i = 0; i < numFeat; i++) {
			utilityFunction.addFeature(i, Math.random()*2.0*avgUtility);
		} */
		
		for(int j = 0; j < numGames; j++) {
			//make adversary (+ give features)
			AbsAdv adversary = new BinarySearch(startGuess, accuracy, utilityFunction);
			adversary.setVerbose(IS_VERBOSE);
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
	/*		System.out.println("Adversary: utility=" + (adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost()));
			System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
			System.out.println("---------------"); 
	*/		
			advSum += adversary.getUtility() + adversary.evaluateUtility() - adversary.getCost(); 
			selSum += selector.getUtility() + selector.evaluateUtility() - selector.getCost();
		}
		myWriter.toBuffer(
				numRounds + ", " +
				threshold + ", " + 
				numFeat + ", " + 
				avgUtility + ", " + 
				startGuess + ", " + 
				accuracy + ", " + 
				(advSum/(double)numGames) + ", " + 
				(selSum/(double)numGames) + "\r\n");
	}

}
