package experiment;

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

public class TrackInformationSharing implements Runnable {

	private static boolean	IS_VERBOSE		= false;
	private static boolean	IS_OVERWRITE	= true;
	private static int		NUM_THREADS		= 10;
	//experiment settings
	private static int		NUM_RUNS				= 1; 
	private static int		NUM_BUYERS				= 8;
	private static int		NUM_LEARN_SELLERS 		= 0; //LearningWithBinarySearch
	private static int		NUM_BASIC_SELLERS 		= 8; //BinarySearch (NOT learning)
	private static boolean	IS_ONE_SALE_PER_ROUND 	= true;
	private static boolean	IS_NAIVE_UTIL_FUNC		= true;
	private static boolean	IS_MULTIPLES			= false;
	private static boolean	IS_WEIGHTED_AVERAGE		= true;
	//witness settings
	private static boolean	IS_INFORMATION_SHARED 	= true;
	private static int 		K_S 					= 8;
	private static double 	ALPHA_S 				= 0.1;	// ignore this. values are assigned in main loop
	private static double 	WITNESS_SCORE_THRESHOLD = 0.1;	// ignore this. values are assigned in main loop
	//q-learning parameters
	private static double	BASE_Q			= 30;
	private static double	EPSILON			= 0.0;
	private static double	LEARNING_RATE	= 0.1;
	private static double	DISCOUNT_FACTOR	= 0.99;
	//game settings
	private static int		NUM_GAMES		= 30;
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
	
	//to avoid file i/o
	//NUM_RUNS x 8 types of values x NUM_GAMES values
	//java assigns all values as 0.0 yay
	//total of all games, later converted to average
	//keep adding what we get after each game, divide by games at end
	static double[][] runsData = new double[NUM_RUNS][8];
	
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

	private static double[] avgData = new double[8];
	private static int[][] witnessParticipationData = new int[NUM_LEARN_SELLERS+NUM_BASIC_SELLERS][NUM_LEARN_SELLERS+NUM_BASIC_SELLERS];
	
	public static synchronized void resetDataAtIndex(int index) {
		avgData[index] = 0;
	}

	public static synchronized void addDataAtIndex(int index, double value) {
		avgData[index] += value;
	}

	public static synchronized void recordWitnessParticipation(int requesterIndex, int witnessIndex) {
		witnessParticipationData[requesterIndex][witnessIndex] += 1;
	}
	
	public static void main(String[] args) {
		// generate different graphs
		ALPHA_S = 0.0;
		K_S = 4;
		_main(args, 1);

		/*
		ALPHA_S = 1.0;
		K_S = 4;
		_main(args, 2);

		ALPHA_S = 1.0;
		K_S = 8;
		_main(args, 3);
		*/
	}
	
	private static void _main(String[] args, int testID) {
		
		for(int k = 0; k <= 20; k++){
			//reset values
			for(int i = 0; i < 8; i++){
				resetDataAtIndex(i);
			}
			
			WITNESS_SCORE_THRESHOLD = k/20.0;
			OUT_PATH = "./outFolder/" + Integer.toString(testID) + "_" + "learningTest" + "_" + Double.toString(WITNESS_SCORE_THRESHOLD).replace(".", "");
			__main(args);
		}
	}
	
	private static void __main(String[] args) {
		//make utility function
		//Constant discountFunction = new Constant(DISCOUNT);
		Linear discountFunction = new Linear(-0.005, 1);
		
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
			Thread temp = new Thread(new TrackInformationSharing());
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

		//get totals
		double avgNumRounds = avgData[F_NUM_ROUNDS];
		double avgThreshold = avgData[F_THRESHOLD];
		double avgNumfeat = avgData[F_NUM_FEAT];
		double avgDiscount = avgData[F_DISCOUNT];
		double avgStartGuess = avgData[F_START_GUESS];
		double avgAccuracy = avgData[F_ACCURACY];
		double avgAdvUtility = avgData[F_ADV_UTILITY];
		double avgSelUtility = avgData[F_SEL_UTILITY];
		
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
	
	public static synchronized int getNextExperimentNum() {
		return ++runNumber;
	}

	public void run() {
		int e = getNextExperimentNum();

		while(e <= NUM_RUNS) {
			System.out.println(Thread.currentThread().getName() + 
					" is running experiment #" + (e));

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

			double[] gameData = new double[8];

			//initialize matrix
			for(int ii = 1; ii < NUM_LEARN_SELLERS+NUM_BASIC_SELLERS; ii++){
				for(int jj = 1; jj < NUM_LEARN_SELLERS+NUM_BASIC_SELLERS; jj++){
					witnessParticipationData[ii][jj] = 0;
				}
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
				Random randomGenerator = new Random(0);
				
				for(int id=0; id<NUM_BUYERS; id++){
					double threshold = randomGenerator.nextGaussian()*EXPECTED_RANGE/2 + THRESHOLD;
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
					int tempIndex = -1;
					
					// each adversary is a list of adversaries maintaining buyer information					
					for(ArrayList<AbsAdv> adversary : adversaryList){					
						tempIndex++;
						
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
							Set<Integer> unknownSet = new HashSet<Integer>();
							unknownSet.addAll(knownSet);
							unknownSet.removeAll(witnessKnownSet);
							double witnessScore;

							if(witnessKnownSet.size() == 0){
								witnessScore = 1;
							}
							else{
								witnessScore = sigmoid(unknownSet.size()/(double)witnessKnownSet.size());
							}

							//participate if score above threshold
							if(witnessScore > witnessScoreThreshold){
								witnesses.add(adversaryIndex);
								
								//recordWitnessParticipation(tempIndex, adversaryIndex);
								witnessParticipationData[tempIndex][adversaryIndex] += 1;
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
				//int maxFace = 1;
				int maxFace = NUM_BUYERS;

				for(int face=0; face<maxFace; face++){
					AbsAdv adversary = (adversaryList.get(0)).get(face);
					advSum += adversary.getUtility();						
				}

				gameData[F_NUM_ROUNDS] += NUM_ROUNDS;
				gameData[F_THRESHOLD] += THRESHOLD;
				gameData[F_NUM_FEAT] += NUM_FEAT;
				gameData[F_DISCOUNT] += DISCOUNT;
				gameData[F_START_GUESS] += START_GUESS;
				gameData[F_ACCURACY] += ACCURACY;
				gameData[F_ADV_UTILITY] += advSum;
				gameData[F_SEL_UTILITY] += selSum;						               
			}

			//print witness matrix
			String witnessOutFile = OUT_PATH.replace("learningTest", "witnessParticipationData") + ".txt";
			Writer witnessWriter = new Writer(witnessOutFile, IS_OVERWRITE);			
			System.out.println("-----printing matrix-----");
			for(int ii = 0; ii < NUM_LEARN_SELLERS+NUM_BASIC_SELLERS; ii++){
				for(int jj = 0; jj < NUM_LEARN_SELLERS+NUM_BASIC_SELLERS; jj++){
					System.out.print(witnessParticipationData[ii][jj]/(double)NUM_GAMES);
					System.out.print(" ");
					witnessWriter.toBuffer(Double.toString(witnessParticipationData[ii][jj]/(double)NUM_GAMES) + ",");
				}
				System.out.println();
			}
			System.out.println("-------------------------");
			witnessWriter.write();

			gameData[F_NUM_ROUNDS] /= NUM_GAMES;
			gameData[F_THRESHOLD] /= NUM_GAMES;
			gameData[F_NUM_FEAT] /= NUM_GAMES;
			gameData[F_DISCOUNT] /= NUM_GAMES;
			gameData[F_START_GUESS] /= NUM_GAMES;
			gameData[F_ACCURACY] /= NUM_GAMES;
			gameData[F_ADV_UTILITY] /= NUM_GAMES;
			gameData[F_SEL_UTILITY] /= NUM_GAMES;						               

			//add to totals
			addDataAtIndex(F_NUM_ROUNDS, gameData[F_NUM_ROUNDS]);
			addDataAtIndex(F_THRESHOLD, gameData[F_THRESHOLD]);
			addDataAtIndex(F_NUM_FEAT, gameData[F_NUM_FEAT]);
			addDataAtIndex(F_DISCOUNT, gameData[F_DISCOUNT]);
			addDataAtIndex(F_START_GUESS, gameData[F_START_GUESS]);
			addDataAtIndex(F_ACCURACY, gameData[F_ACCURACY]);
			addDataAtIndex(F_ADV_UTILITY, gameData[F_ADV_UTILITY]);
			addDataAtIndex(F_SEL_UTILITY, gameData[F_SEL_UTILITY]);
			
			e = getNextExperimentNum();//get next experiment number
		}		
	}
}
