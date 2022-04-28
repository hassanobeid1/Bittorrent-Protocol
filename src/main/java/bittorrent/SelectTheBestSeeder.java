package bittorrent;

import java.util.*;

public class SelectTheBestSeeder {

	private static int nbSeeders = 6; //TODO : change it to the real number of seeders (the tracker tells us the number)
	private static int nbPieces = 7;  //TODO : change it to the real number of pieces (the length of Bitfield tells us the number)
	// private static int[][] seedersWithTheirBitfields;
	private static int[] nbOnesForEachSeeder;
	private static int[] nbOfOnesVertically;
	private static int nbDifferentElementsOfVerticalArray;

	private static Map<String,Integer> nbPiecesTransmittingBySeeders;
	private static Map<String,Integer> auxNbOnesTransmittingBySeeders;
	private static Map<String,Integer> auxMap;
	private static Map<String,Integer> aux_sorted_Nb_Pieces_Transmitting_By_Seeders;
	//TODO : decrement the number when we get a piece
	private static Map<String,Integer> seederNbOnesMap; // <seeder, number of ones for this seeder>

	/**
	 * Function used to find the minimum in an array
	 * @param arr : the array
	 * @return : the minimum
	 */
	public static int getMin(int[] arr){

		int min = arr[0];

		for (int i = 0; i < arr.length; i++) {
			if(arr[i] <min)
				min = arr[i];
		}
		return min;
	}


	/**
	 * Function to find the minimum VALUE in a map
	 * @param mapOfValues : the map
	 * @return the minimum VALUE of the map
	 */
	public static int min_value_map(TreeMap<String, Integer> mapOfValues) {

		Map.Entry<String, Integer> minEntry = null;

		for (Map.Entry<String, Integer> entry : mapOfValues.entrySet()) {

			if (minEntry == null || minEntry.getValue() > entry.getValue()) {
				minEntry = entry;
			}
		}

		return minEntry.getValue();
	}


	/**
	 * Function used to know how many times we should loop over the nbOfOnesVertically array
	 * @param arr : the array
	 * @return the number of different elements in an array
	 */
	public static int countDifferentElements (int arr[]) {

		int res = 0;
		for (int i = 0; i < arr.length; i++){
			int j;
			for (j = 0; j < i; j++)
				if (arr[i] == arr[j])
					break;

			if (i == j){
				res++;
			}
		}

		return res;
	}


	/**
	 * The algorithm of sending the requests to the seeders
	 */
	public static void sendingRequestsToSeedersAlgorithm() {
		int[][] seedersWithTheirBitfields =  {{1,1,1,1,1,0,0}, {1,0,1,1,0,1,0}, {1,0,0,1,0,0,1}, {0,0,1,1,0,0,0}, {0,0,1,0,0,0,0}, {0,0,0,1,0,0,0}};   //new boolean[nbSeeders][nbPieces]; //TODO : fill this matrix with the bitfield of each seeder
		
		nbOnesForEachSeeder = new int[nbSeeders];
		nbOfOnesVertically = new int[nbPieces];
		seederNbOnesMap = new TreeMap<String,Integer>();
		nbPiecesTransmittingBySeeders = new TreeMap<String,Integer>();
		auxNbOnesTransmittingBySeeders = new TreeMap<String,Integer>();
		auxMap = new TreeMap<String,Integer>();
		aux_sorted_Nb_Pieces_Transmitting_By_Seeders = new TreeMap<String,Integer>();


		///Used to sort the map by VALUES
		ValueComparator bvc = new ValueComparator(seederNbOnesMap);
		ValueComparator bvc2 = new ValueComparator(seederNbOnesMap);
		TreeMap<String, Integer> sorted_map_for_ones = new TreeMap<String, Integer>(bvc);
		TreeMap<String, Integer> sorted_Nb_Pieces_Transmitting_By_Seeders = new TreeMap<String, Integer>(bvc2);

		///Filling arrays with 0
		Arrays.fill(nbOnesForEachSeeder, 0);
		Arrays.fill(nbOfOnesVertically, 0);

		//Counting the ones in the matrix
		for(int i = 0; i < seedersWithTheirBitfields.length; i++)
			for(int j = 0; j < seedersWithTheirBitfields[i].length; j++)

				if(seedersWithTheirBitfields[i][j] == 1) {
					nbOnesForEachSeeder[i]++;
					nbOfOnesVertically[j]++;
				}


		//Initialization of nbPiecesTransmittingBySeeders to 0 for each seeder
		for(int i = 0; i < nbSeeders; i++)
			nbPiecesTransmittingBySeeders.put(Integer.toString(i), 0);


		//Counting the number of different element in nbOfOnesVertically to know how many times the loop must iterate
		nbDifferentElementsOfVerticalArray = countDifferentElements(nbOfOnesVertically);


		for (int i = 0; i < nbDifferentElementsOfVerticalArray; i++) {
			int min = getMin(nbOfOnesVertically);

			for(int j = 0; j < nbPieces; j++)
				if(nbOfOnesVertically[j] == min) {

					//Scan the matrix vertically to know which seeder has this piece
					for(int k = 0; k < nbSeeders; k++)
						if(seedersWithTheirBitfields[k][j] == 1)
						{
							seederNbOnesMap.put(Integer.toString(k), nbOnesForEachSeeder[k]);
							aux_sorted_Nb_Pieces_Transmitting_By_Seeders.put(Integer.toString(k), nbPiecesTransmittingBySeeders.get(Integer.toString(k)));
						}



					//Sorting by value to send the requests to the seeder having the minimum of ones
					sorted_map_for_ones.putAll(seederNbOnesMap);
					sorted_Nb_Pieces_Transmitting_By_Seeders.putAll(aux_sorted_Nb_Pieces_Transmitting_By_Seeders);
					System.out.println("sorted_Nb_Pieces_Transmitting_By_Seeders "+ sorted_Nb_Pieces_Transmitting_By_Seeders);

					//Putting in auxMap all seeders having this piece and having the same minimum of number of pieces in transmission
					Iterator<Map.Entry<String, Integer>> it = sorted_Nb_Pieces_Transmitting_By_Seeders.entrySet().iterator();
					Map.Entry<String, Integer> firstValue = null;
					if(it != null)
						firstValue = it.next();

					int minNbPiecesTransmitting = firstValue.getValue();
					

					//SubMap to get the seeders sending the minimum number of packets
					Iterator<Map.Entry<String, Integer>> iterator1 = sorted_Nb_Pieces_Transmitting_By_Seeders.entrySet().iterator();
					while((iterator1.hasNext()))
					{
						Map.Entry<String, Integer> actualValue1 = iterator1.next();
						if(actualValue1.getValue() == minNbPiecesTransmitting && seederNbOnesMap.containsKey(actualValue1.getKey()))
							auxMap.put(actualValue1.getKey(), actualValue1.getValue());
					}
					System.out.println("Here " + auxMap);

					//Choose which seeder will send the packet
					Map.Entry<String, Integer> actualValue2 = null;
					Map.Entry<String, Integer> actualValue3 = null;
					Iterator<Map.Entry<String, Integer>> iterator2 = sorted_map_for_ones.entrySet().iterator();
					System.out.println("sorted_map_for_ones " + sorted_map_for_ones);
					boolean in = true;
					while((iterator2.hasNext()) && in)
					{
						iterator1 = auxMap.entrySet().iterator();
						actualValue2 = iterator2.next();
						actualValue3 = iterator1.next();

						if(auxMap.size() == 1)
							actualValue2 = actualValue3;

						else
							while(iterator1.hasNext())
								if(!actualValue2.getKey().equals(actualValue3.getKey()))
									actualValue3 = iterator1.next();
								else
								{
									in = false;
									break;
								}
					}



					nbPiecesTransmittingBySeeders.put(actualValue2.getKey(), nbPiecesTransmittingBySeeders.get(actualValue2.getKey()) + 1);
					sorted_Nb_Pieces_Transmitting_By_Seeders.put(actualValue2.getKey(), nbPiecesTransmittingBySeeders.get(actualValue2.getKey()) + 1);
					System.out.println("Seeder number " + actualValue2.getKey() + " has to send piece number " + j +", by now it\'s sending " + nbPiecesTransmittingBySeeders.get(actualValue2.getKey()) + " pieces"   + "\n");

					/****Send the request****/
					//System.out.println(value);

					seederNbOnesMap.clear();
					sorted_map_for_ones.clear();
					auxNbOnesTransmittingBySeeders.clear();
					auxMap.clear();
					sorted_Nb_Pieces_Transmitting_By_Seeders.clear();
					aux_sorted_Nb_Pieces_Transmitting_By_Seeders.clear();


					//Without this instruction, we will have always the same minimum
					nbOfOnesVertically[j] = Integer.MAX_VALUE;

				}
		}
	}


	public static void main(String[] args) {

		sendingRequestsToSeedersAlgorithm();

	}

}
