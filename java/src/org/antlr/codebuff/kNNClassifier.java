package org.antlr.codebuff;

import org.antlr.v4.runtime.misc.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A kNN (k-Nearest Neighbor) classifier */
public abstract class kNNClassifier {
	protected List<int[]> X;
	protected List<Integer> Y;
	protected boolean[] categorical;
//	public final int numCategories;
	public boolean dumpVotes = false;

	public class Neighbor {
		public final int category;
		public final double distance;
		public final int corpusVectorIndex;

		public Neighbor(int category, double distance, int corpusVectorIndex) {
			this.category = category;
			this.distance = distance;
			this.corpusVectorIndex = corpusVectorIndex;
		}

		@Override
		public String toString() {
//			return String.format("(@%d,cat=%d,d=%1.2f)", corpusVectorIndex, category, distance);
			String features = CollectFeatures._toString(X.get(corpusVectorIndex));
			return String.format("%s (cat=%d,d=%1.2f)", features, category, distance);
		}
	}

	public kNNClassifier(List<int[]> X, List<Integer> Y, boolean[] categorical) {
		this.X = X;
		this.Y = Y;
		this.categorical = categorical;
//		numCategories = max(Y) + 1;
	}

	public int classify(int k, int[] unknown) {
		return classify(k, unknown, 1.0);
	}

	/** Walk all training samples and compute distance(). Return indexes of k
	 *  smallest distance values.  Categories can be any negative or positive
	 *  integer (and 0).
	 */
	public int classify(int k, int[] unknown, double distanceThreshold) {
		HashBag<Integer> votes = votes(k, unknown, distanceThreshold);
		int max = Integer.MIN_VALUE;
		int catWithMostVotes = 0;
		for (Integer category : votes.keySet()) {
			if ( votes.get(category) > max ) {
				max = votes.get(category);
				catWithMostVotes = category;
			}
		}

		return catWithMostVotes;
	}

	public HashBag<Integer> votes(int k, int[] unknown) {
		return votes(k, unknown, 1.0);
	}

	public HashBag<Integer> votes(int k, int[] unknown, double distanceThreshold) {
		Neighbor[] kNN = kNN(k, unknown);
		HashBag<Integer> votes = new HashBag<>();
		Map<Integer, List<Integer>> charPos = new HashMap<>();
		Map<Integer, List<Integer>> widths = new HashMap<>();
		Map<Integer, List<Integer>> sum = new HashMap<>();
//		List<Integer>[] charPos = new List[numCategories];
//		List<Integer>[] widths = new List[numCategories];
//		List<Integer>[] sum = new List[numCategories];
		for (int i=0; i<k && i<kNN.length; i++) {
			// Don't count any votes for training samples too distant.
			if ( kNN[i].distance > distanceThreshold ) break;
			votes.add(kNN[i].category);
//			int[] features = X.get(kNN[i].corpusVectorIndex);
//			charPos[kNN[i].category].add(features[CollectFeatures.INDEX_PREV_END_COLUMN]);
//			widths[kNN[i].category].add(features[CollectFeatures.INDEX_ANCESTOR_WIDTH]);
//			sum[kNN[i].category].add(features[CollectFeatures.INDEX_PREV_END_COLUMN]+
//									 features[CollectFeatures.INDEX_ANCESTOR_WIDTH]);
		}
		if ( dumpVotes ) {
			System.out.println(toString(unknown)+"->"+votes);
			kNN = Arrays.copyOfRange(kNN, 0,16);
			System.out.println(Utils.join(kNN, "\n"));
		}
//		System.out.println(Arrays.toString(charPos));
//		System.out.println(Arrays.toString(widths));
//		System.out.println(Arrays.toString(sum));

		return votes;
	}

	public Neighbor[] kNN(int k, int[] unknown) {
		Neighbor[] distances = distances(k, unknown);
		Arrays.sort(distances,
		            (Neighbor o1, Neighbor o2) -> Double.compare(o1.distance,o2.distance));
		return Arrays.copyOfRange(distances, 0, k);
	}

	public Neighbor[] distances(int k, int[] unknown) {
		int n = X.size(); // num training samples
		Neighbor[] distances = new Neighbor[n];
		for (int i=0; i<n; i++) {
			int[] x = X.get(i);
			distances[i] = new Neighbor(Y.get(i), distance(x, unknown), i);
		}
		return distances;
	}

	public abstract double distance(int[] A, int[] B);

	public abstract String toString(int[] features);
}