package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import utilityFunc.*;
import utilityFunc.discountFunc.*;
import agent.adversary.*;
import agent.selector.*;

public class MultithreadExperiment implements Runnable {

	private static boolean	IS_VERBOSE		= false;
	private static boolean	IS_OVERWRITE	= true;
	private static int		NUM_THREADS		= 10;
	//experiment settings
	private static int		NUM_RUNS				= 1000; 
	private static int		NUM_BUYERS				= 8;
	private static int		NUM_LEARN_SELLERS 		= 8; //LearningWithBinarySearch
	private static int		NUM_BASIC_SELLERS 		= 0; //BinarySearch (NOT learning)
	private static boolean	IS_ONE_SALE_PER_ROUND 	= true;
	private static boolean	IS_NAIVE_UTIL_FUNC		= true;
	private static boolean	IS_MULTIPLES			= false;
	private static boolean	IS_WEIGHTED_AVERAGE		= true;
	//witness settings
	private static boolean	IS_INFORMATION_SHARED 	= true;
	private static int 		K_S 					= 5;
	private static double 	ALPHA_S 				= 0.1;	// ignore this. values are assigned in main loop
	private static double 	WITNESS_SCORE_THRESHOLD = 0.1;	// ignore this. values are assigned in main loop
	//q-learning parameters
	private static double	BASE_Q			= 30;
	private static double	EPSILON			= 0.0;
	private static double	LEARNING_RATE	= 0.1;
	private static double	DISCOUNT_FACTOR	= 0.99;
	//game settings
	private static int		NUM_GAMES		= 300;
	private static int	 	NUM_ROUNDS		= 40;
	private static double 	THRESHOLD 		= 50;
	private static int		NUM_FEAT		= 8; 
	private static double	DISCOUNT		= 0.5;
	private static double	START_GUESS		= 85.001;
	private static double	EXPECTED_RANGE	= 10;
	private static double	ACCURACY		= 0.95;
	//io paths
	private static String	IN_PATH = "./input/letter_dataset/letter";
	private static String	OUT_PATH	= "./outFolder/learningTest" + "_" + Double.toString(WITNESS_SCORE_THRESHOLD).replace(".", "");
	//indices to different parts of the data in file
	private static int		F_NUM_ROUNDS	= 0;
	private static int		F_THRESHOLD		= 1;
	private static int		F_NUM_FEAT		= 2;
	private static int		F_DISCOUNT		= 3;
	private static int		F_START_GUESS	= 4;
	private static int		F_ACCURACY		= 5;
	private static int		F_ADV_UTILITY	= 6;
	private static int		F_SEL_UTILITY	= 7;
	
	private static double sigmoid(double x)
	{
	    return 1 / (1 + Math.exp(-x));
	}
	//java does not support type conversion so we need this for speed
	@SuppressWarnings("serial")
	private static HashMap<Boolean, Integer> boolToInt = new HashMap<Boolean, Integer>() {{
		put(true, 1);
		put(false, 0);
	}};

	private static int runNumber; //used to allocate experiments to threads on a First-come-first-served basis
	private static AbsUtF utilityFunction; //single global utility function
	
	public static void main(String[] args) {
		
		for(int k = 0; k <= 10; k++){
			ALPHA_S = 0.9;
			WITNESS_SCORE_THRESHOLD = k/10.0;
			OUT_PATH = "./outFolder/learningTest" + "_" + Double.toString(WITNESS_SCORE_THRESHOLD).replace(".", "");
			_main(args);			
		}
	}
	
	private static void _main(String[] args) {
		//make utility function
		Constant discountFunction = new Constant(DISCOUNT);
		if(!IS_NAIVE_UTIL_FUNC) {
			utilityFunction = new NDimen(discountFunction, IS_MULTIPLES, IN_PATH, 1);
		}
		else {
			utilityFunction = new Naive1D(discountFunction, IS_MULTIPLES, IN_PATH);
		}
		runNumber = 0; //no experiments done yet
		
		long startTime = System.currentTimeMillis();
		//start all threads
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		for(int e = 0; e < NUM_THREADS; e++) {
			Thread temp = new Thread(new MultithreadExperiment());
			threadList.add(temp);
			temp.start();
		}
		for(Thread curThread : threadList) {
			try {
				curThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("Time taken (minutes): " + (stopTime - startTime)/60000d);

		//set up file writer
		Writer mainWriter;
		mainWriter = new Writer(OUT_PATH + ".txt", IS_OVERWRITE);
		mainWriter.toBuffer("numRounds, threshold, numFeat, DISCOUNT, startGuess, accuracy, advUtility, selUtility\r\n");
		mainWriter.write();

		//open files to compute average
		BufferedReader[] myBufferReader = new BufferedReader[NUM_RUNS];
		String[] fileBuffer = new String[NUM_RUNS];

		try {
			for(int e = 0; e < NUM_RUNS; e++) {
				//open buffer
				myBufferReader[e] = new BufferedReader(new FileReader(OUT_PATH + "_" + (e+1) + ".txt"));
			}

			while (true) {
				//read one line
				int avgNumRounds = 0;
				double avgThreshold = 0;
				int avgNumfeat = 0;
				double avgDiscount = 0;
				double avgStartGuess = 0;
				double avgAccuracy = 0;
				double avgAdvUtility = 0;
				double avgSelUtility = 0;

				for(int e = 0; e < NUM_RUNS; e++) {
					try {								
						//check end of file
						fileBuffer[e] = myBufferReader[e].readLine();
						if(fileBuffer[e] == null){
							//delete all individual files
							for(int f = 0; f < NUM_RUNS; f++) {
								File file = new File(OUT_PATH + "_" + (f+1) + ".txt");
								file.delete();
							}

							//end of averaging
							System.out.println("Done processing.");
							return;
						}

						String[] tokenizedBuffer = fileBuffer[e].split(", ");

						avgNumRounds += Integer.parseInt(tokenizedBuffer[F_NUM_ROUNDS]);
						avgThreshold += Double.parseDouble(tokenizedBuffer[F_THRESHOLD]);
						avgNumfeat += Integer.parseInt(tokenizedBuffer[F_NUM_FEAT]);
						avgDiscount += Double.parseDouble(tokenizedBuffer[F_DISCOUNT]);
						avgStartGuess += Double.parseDouble(tokenizedBuffer[F_START_GUESS]);
						avgAccuracy += Double.parseDouble(tokenizedBuffer[F_ACCURACY]);
						avgAdvUtility += Double.parseDouble(tokenizedBuffer[F_ADV_UTILITY]);
						avgSelUtility += Double.parseDouble(tokenizedBuffer[F_SEL_UTILITY]);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				//compute average
				avgNumRounds /= NUM_RUNS;
				avgThreshold /= NUM_RUNS;
				avgNumfeat /= NUM_RUNS;
				avgDiscount /= NUM_RUNS;
				avgStartGuess /= NUM_RUNS;
				avgAccuracy /= NUM_RUNS;
				avgAdvUtility /= NUM_RUNS;
				avgSelUtility /= NUM_RUNS;

				mainWriter.toBuffer(
						avgNumRounds + ", " +
								avgThreshold + ", " + 
								avgNumfeat + ", " + 
								avgDiscount + ", " + 
								avgStartGuess + ", " + 
								avgAccuracy + ", " + 
								avgAdvUtility + ", " + 
								avgSelUtility + "\r\n");
				mainWriter.write();
			}				
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}	
	}

	public static synchronized int getNextExperimentNum() {
		return ++runNumber;
	}

	public void run() {
		int e = getNextExperimentNum();
		while(e <= NUM_RUNS) {
			System.out.println(Thread.currentThread().getName() + 
					" is running experiment #" + (e));

			//set up file writing
			Writer myWriter = new Writer(OUT_PATH + "_" + (e) + ".txt", IS_OVERWRITE);

			double advSum = 0;
			double selSum = 0;

			/* set up sellers */
			//each seller maintains individual binary search instances for individual sellers
			//a seller is then represented as an array of binary search objects
			ArrayList<ArrayList<AbsAdv>> adversaryList = new ArrayList<ArrayList<AbsAdv>>();

			for(int k = 0; k < NUM_LEARN_SELLERS; k++) {
				ArrayList<AbsAdv> lwbsAdversary = new ArrayList<AbsAdv>();
				for(int id=0; id<NUM_BUYERS; id++){
					AbsAdv adversaryEntity = new LearningWithBinarySearch(START_GUESS, ACCURACY, utilityFunction, 
							BASE_Q, EPSILON, LEARNING_RATE, DISCOUNT_FACTOR, IS_WEIGHTED_AVERAGE, EXPECTED_RANGE);			
					adversaryEntity.setVerbose(IS_VERBOSE);
					lwbsAdversary.add(adversaryEntity);
				}
				adversaryList.add(lwbsAdversary);
			}

			for(int j = 0; j < NUM_GAMES; j++) {

				//remove everything from adversaryList if the entry is type BS
				//add a new entry for BS
				//copying to a new object for now
				ArrayList<ArrayList<AbsAdv>> newAdversaryList = new ArrayList<ArrayList<AbsAdv>>();
				for(ArrayList<AbsAdv> adversaryEntities : adversaryList){
					if(adversaryEntities.get(0).getClass().equals(LearningWithBinarySearch.class)){
						newAdversaryList.add(adversaryEntities);
					}
				}
				adversaryList.clear();
				adversaryList = newAdversaryList;

				for(int k = 0; k < NUM_BASIC_SELLERS; k++) {
					ArrayList<AbsAdv> bsAdversary = new ArrayList<AbsAdv>();
					for(int id=0; id<NUM_BUYERS; id++){
						AbsAdv adversaryEntity = new BinarySearch(START_GUESS, ACCURACY, utilityFunction, IS_WEIGHTED_AVERAGE, EXPECTED_RANGE);
						adversaryEntity.setVerbose(IS_VERBOSE);
						bsAdversary.add(adversaryEntity);
					}
					adversaryList.add(bsAdversary);
				}

				/* set up buyers */
				//make selector
				//we now maintain a list of buyers, which do not model the sellers
				//if we wish to model the sellers, then one buyer can be represented as multiple buyers
				//in a manner similar to seller implementation
				ArrayList<AbsSel> selectorList = new ArrayList<AbsSel>();
				Random randomGenerator = new Random();
				
				for(int id=0; id<NUM_BUYERS; id++){
					double threshold = randomGenerator.nextGaussian()*EXPECTED_RANGE + THRESHOLD;
					AbsSel selector = new UtilityThreshold(threshold, utilityFunction);
					selector.setVerbose(IS_VERBOSE);
					selectorList.add(selector);
				}			

				// each adversary is a list of adversaries maintaining buyer information
				// reset adversaries
				for(ArrayList<AbsAdv> adversaryEntities : adversaryList){
					for(AbsAdv adversaryEntity : adversaryEntities){
						if(adversaryEntity.getClass().equals(LearningWithBinarySearch.class)){
							((LearningWithBinarySearch)adversaryEntity).resetForLearning(START_GUESS);
						}

						adversaryEntity.setVerbose(IS_VERBOSE);
						if(IS_VERBOSE) System.out.println("Game #" + (j+1));

						for(int i = 1; i <= NUM_FEAT; i++) {
							adversaryEntity.addFeature(i);
						}
					}
				}

				//run random number of rounds
				for(int i = 0; i < NUM_ROUNDS; i++) {
					// each adversary is a list of adversaries maintaining buyer information
					
					// karan: randomize adversary list here
					Collections.shuffle(adversaryList);
					
					for(ArrayList<AbsAdv> adversary : adversaryList){					

						/* information gathering */
						//gather information about sellers from other adversaries
						int k_s = K_S;
						double alpha_s = ALPHA_S;
						double witnessScoreThreshold = WITNESS_SCORE_THRESHOLD;

						//select order of requesting adversaries randomly
						ArrayList<Integer> adversaryIndices = new ArrayList<Integer>();
						for(int k = 0; k < adversaryList.size(); k++){
							if(k != adversaryList.indexOf(adversary)){
								adversaryIndices.add(k);
							}
						}
						Collections.shuffle(adversaryIndices);

						//buyers that this adversary knows about are those for which interaction > 0
						// we may later use these number for more complicated methods
						Set<Integer> knownSet = new HashSet<Integer>();
						for(int index = 0; index < adversary.size(); index++){
							if(adversary.get(index).getInteractionCount() > 0){
								knownSet.add(index);
							}
						}

						//iterate through candidate witness adversaries
						Set<Integer> witnesses = new HashSet<Integer>();
						for(int adversaryIndex : adversaryIndices){
							Set<Integer> witnessKnownSet = new HashSet<Integer>();
							for(int index = 0; index < adversaryList.get(adversaryIndex).size(); index++){
								if(adversaryList.get(adversaryIndex).get(index).getInteractionCount() > 0){
									witnessKnownSet.add(index);
								}
							}

							//compute score for this witness
							witnessKnownSet.removeAll(knownSet);
							Set<Integer> unknownSet = witnessKnownSet;
							double witnessScore;

							if(knownSet.size() == 0){
								witnessScore = Double.POSITIVE_INFINITY;
							}
							else{
								witnessScore = sigmoid(unknownSet.size()/knownSet.size());
							}

							//participate if score above threshold
							if(witnessScore > witnessScoreThreshold){
								witnesses.add(adversaryIndex);
							}

							//we are done if we reach the limit on number of witnesses
							if(witnesses.size() >= k_s){
								break;
							}
						}

						//now use witness information to condition adversary model of selectors
						for(int k = 0; k < selectorList.size(); k++){
							double lowerPredictionEstimate = Double.MIN_VALUE;
							double upperPredictionEstimate = Double.MAX_VALUE;
							int contributingWitnessCount = 0;

							for(Integer witness : witnesses){
								//only contributes about those buyers that the witness has interacted with
								if(adversaryList.get(witness).get(k).getInteractionCount() > 0){
									contributingWitnessCount++;
									List<Double> modelEstimate = adversaryList.get(witness).get(k).getModelEstimate();

									//not taking average; instead, using witness with most info
									if(modelEstimate.get(0) > lowerPredictionEstimate)
										lowerPredictionEstimate = modelEstimate.get(0);
									if(modelEstimate.get(1) < upperPredictionEstimate)
										upperPredictionEstimate = modelEstimate.get(1);
									//lowerPredictionEstimate += modelEstimate.get(0);
									//upperPredictionEstimate += modelEstimate.get(1);
								}
							}

							if(contributingWitnessCount > 0){
								//no need for average since the most extreme value is taken
								//lowerPredictionEstimate /= contributingWitnessCount;
								//upperPredictionEstimate /= contributingWitnessCount;

								//condition based on information sharing
								//faster than an if condition
								alpha_s = alpha_s*boolToInt.get(IS_INFORMATION_SHARED);

								//account for adversary's own model
								lowerPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(0) + alpha_s*lowerPredictionEstimate;
								upperPredictionEstimate = (1 - alpha_s)*adversary.get(k).getModelEstimate().get(1) + alpha_s*upperPredictionEstimate;

								//use these estimates to generate cost
								//these changes are currently permanent to the adversary
								//they may later be made temporary per round							
								adversary.get(k).setModelEstimate(Arrays.asList(lowerPredictionEstimate, upperPredictionEstimate));
							}
						}
						/* end of information gathering */

						//select selectors randomly
						ArrayList<Integer> selectorIndices = new ArrayList<Integer>();
						for(int k = 0; k < selectorList.size(); k++){
							selectorIndices.add(k);
						}
						Collections.shuffle(selectorIndices);

						// iterate through selectors
						for(int selectorIndex : selectorIndices){

							// try to sell to that buyer
							boolean accepted = adversary.get(selectorIndex).giveOffer(selectorList.get(selectorIndex));

							if(accepted == true && IS_ONE_SALE_PER_ROUND){
								// seller's turn is over if item sold
								break;
							}
						}
					}				
				}

				//output total utility 
				//AbsAdv adversary = (adversaryList.get(0)).get(0);
				//System.out.println("\n" + adversary.getUtility() + " " + adversary.evaluateUtility() + " " + adversary.getCost());			
				//System.out.println("Adversary: utility=" + (adversary.getUtility()/* + adversary.evaluateUtility() - adversary.getCost()*/));
				//System.out.println("Selector:  utility=" + (selector.getUtility() + selector.evaluateUtility() - selector.getCost()));
				//System.out.println("---------------"); 
				//((LearningWithBinarySearch)adversary).dumpPolicy();

				//advSum = adversary.getUtility(); 
				//selSum = selector.getUtility() - selector.getCost();
				
				advSum = 0;
				int maxFace = 1;
				//maxFace = NUM_BUYERS;

				for(int face=0; face<maxFace; face++){
					AbsAdv adversary = (adversaryList.get(0)).get(face);
					advSum += adversary.getUtility();						
				}
				myWriter.toBuffer(
						NUM_ROUNDS + ", " +
								THRESHOLD + ", " + 
								NUM_FEAT + ", " + 
								DISCOUNT + ", " + 
								START_GUESS + ", " + 
								ACCURACY + ", " + 
								advSum + ", " + 
								selSum + "\r\n");
			}
			myWriter.write();
			e = getNextExperimentNum();//get next experiment number
		}
	}
}
