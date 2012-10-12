package strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import model.Sample;
import model.SampleParser;
import model.SampleSet;

public class DecisionTreeStrategyC45 implements DecisionTreeStrategy {

	@Override
	public SplittingCriterion selectAttributeForSplitting(SampleSet dataset,
			List<Integer> attributeList) {
		SplittingCriterion maxResult = new SplittingCriterion();
		maxResult.value = Double.MIN_VALUE;
		List<SplittingCriterion> list = new ArrayList<SplittingCriterion>();
		for (int i = 0; i < attributeList.size(); i++) {
			List<SplittingCriterion> r = computeEntropy(dataset,
					attributeList.get(i));
			list.addAll(r);
		}
		double avgGain = 0;
		for (SplittingCriterion sc : list) {
			avgGain += sc.value1;
		}
		avgGain /= list.size();
		for (SplittingCriterion sc : list) {
			if (sc.value1 >= avgGain) {
				if (sc.value > maxResult.value) {
					maxResult = sc;
				}
			}
		}
		return maxResult;
	}

	@Override
	public void prune(TreeNode tree, DecisionTree decisionTree) {
		// TODO
		// use cost-complexity pruning here
		// because the fault rate is zero if using the given training set
		List<Sample> testSet = decisionTree.trainingSet
				.getSortedListByAttribute(0);

//		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
		for (Sample s : testSet) {
			ClassifyInfo info = classifyStep((DefaultMutableTreeNode) tree, s);
			updateNode(info.node, info.label == s.getLabel(), 1);
		}
		pruneStep((DefaultMutableTreeNode) tree, decisionTree.trainingSet.getSortedListByAttribute(0));

	}
	
	List<Sample>[] split(SplittingCriterion criterion,List<Sample> samples)
	{
		@SuppressWarnings("unchecked")
		List<Sample>[] lists=new ArrayList[criterion.splitPoints.length+1];
		for (int i=0;i<lists.length;i++)
			lists[i]=new ArrayList<Sample>();
		for (Sample s:samples)
		{
			int p=criterion.getPosition(s);
			lists[p].add(s);
		}
		return lists;
	}

	void pruneStep(TreeNode tree, List<Sample> trainList) {
		 DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
		 double estimate = estimate(node) + 0.5;
		 SplittingCriterion criterion = (SplittingCriterion) node
		 .getUserObject();
		 double currentEstimate = criterion.value1
		 + node.getLeafCount()/2.0
		 + se(criterion.value1 + node.getLeafCount()/2.0,
				 trainList.size());
		 if (estimate < currentEstimate)
		 {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree;
			SplittingCriterion c = new SplittingCriterion();
			c.attr = majorityLabel(n);
			n.setUserObject(c);
			n.removeAllChildren();
		 }
		 else
		 {
			 List<Sample>[] lists=split(criterion, trainList);
			 for (int i=0;i<node.getChildCount();i++)
				 pruneStep(node.getChildAt(i), lists[i]);
		 }

//		double errorsLargestBranch;
//		double errorsLeaf;
//		double errorsTree;
//		TreeNode largestBranch;
//		int i;
//
//		if (!tree.isLeaf()) {
//
//			// Prune all subtrees.
//			for (i = 0; i < tree.getChildCount(); i++)
//				prune(tree.getChildAt(i), decisionTree);
//
//			// Compute error for largest branch
//			largestBranch = getLargestBranch(tree);
//			// indexOfLargestBranch = localModel().distribution().maxBag();
//			boolean m_subtreeRaising = true;
//			// if (m_subtreeRaising) {
//			// errorsLargestBranch =
//			// .getEstimatedErrorsForBranch(largestBranch,decisionTree.trainingSet);
//			// } else {
//			errorsLargestBranch = Double.MAX_VALUE;
//			// }
//
//			// Compute error if this Tree would be leaf
//			errorsLeaf = getEstimatedErrorsForDistribution((DefaultMutableTreeNode) tree);
//
//			// Compute error for the whole subtree
//			errorsTree = getEstimatedErrors((DefaultMutableTreeNode) tree);
//
//			// Decide if leaf is best choice.
//			if (smOrEq(errorsLeaf, errorsTree + 0.1)
//					&& smOrEq(errorsLeaf, errorsLargestBranch + 0.1)) {
//
//				// Free son Trees
//				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
//				SplittingCriterion criterion = new SplittingCriterion();
//				criterion.attr = majorityLabel(node);
//				node.setUserObject(criterion);
//				node.removeAllChildren();
//
//				// Get NoSplit Model for node.
//				return;
//			}
//
//			// Decide if largest branch is better choice
//			// than whole subtree.
//			if (smOrEq(errorsLargestBranch, errorsTree + 0.1)) {
//				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree;
//				if (node.getParent() == null)
//					decisionTree.tree = new DefaultTreeModel(largestBranch);
//				else {
//					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
//							.getParent();
//					int index = parent.getIndex(node);
//					parent.remove(index);
//					parent.insert((MutableTreeNode) largestBranch, index);
//				}
//				prune(largestBranch, decisionTree);
//			}
//		}

	}

	int estimate(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode leaf = node.getFirstLeaf();
		class Info
		{
			int count=1;
			int correctCount=0;
			int faultCount=0;
		}
		Map<Integer, Info> map = new HashMap<Integer, Info>();
		while (leaf != null && leaf.isNodeAncestor(node)) {
			SplittingCriterion criterion = (SplittingCriterion) leaf
					.getUserObject();
			int key = criterion.attr;
			if (map.containsKey(key)) {
				Info info = map.get(key);
				info.count++;
				info.correctCount+=criterion.value;
				info.faultCount+=criterion.value1;
			} else {
				Info info=new Info();
				info.correctCount+=criterion.value;
				info.faultCount+=criterion.value1;
				map.put(key, info);
			}
			leaf = leaf.getNextLeaf();
		}
		int label=0;
		int maxCount = 0;
		for (Map.Entry<Integer, Info> e : map.entrySet()) {
			if (e.getValue().count > maxCount) {
				maxCount = e.getValue().count;
				label = e.getKey();
			}
		}
		int fault=0;
		for (Map.Entry<Integer, Info> e : map.entrySet()) {
			if (e.getKey()==label)
			{
				fault+=e.getValue().faultCount;
			}
			else
				fault+=e.getValue().correctCount;
		}
		return fault;
	}

	double se(double e, int n) {
		return Math.sqrt((double) e * (n - e) / n);
	}

//	static double SMALL = 1e-6;
//
//	public static/* @pure@ */boolean smOrEq(double a, double b) {
//
//		return (a - b < SMALL);
//	}
//
//	TreeNode getLargestBranch(TreeNode node) {
//		return null;
//	}
//
//	private double getEstimatedErrors(DefaultMutableTreeNode node) {
//
//		double errors = 0;
//		int i;
//
//		if (node.isLeaf())
//			return getEstimatedErrorsForDistribution(node);
//		else {
//			for (i = 0; i < node.getChildCount(); i++)
//				errors = errors
//						+ getEstimatedErrors((DefaultMutableTreeNode) node
//								.getChildAt(i));
//			return errors;
//		}
//	}
//
//	private double getEstimatedErrorsForDistribution(DefaultMutableTreeNode node) {
//
//		SplittingCriterion criterion = (SplittingCriterion) node
//				.getUserObject();
//		if (criterion.value + criterion.value1 == 0)
//			return 0;
//		else
//			return criterion.value1
//					+ addErrs(criterion.value + criterion.value1,
//							criterion.value1, 0.25);
//	}
//
//	// int attributeNum(TreeModel tree) {
//	// return 0;
//	// }
//
//	private double addErrs(double N, double e, double CF) {
//
//		// Ignore stupid values for CF
//		if (CF > 0.5) {
//			System.err.println("WARNING: confidence value for pruning "
//					+ " too high. Error estimate not modified.");
//			return 0;
//		}
//
//		// Check for extreme cases at the low end because the
//		// normal approximation won't work
//		if (e < 1) {
//
//			// Base case (i.e. e == 0) from documenta Geigy Scientific
//			// Tables, 6th edition, page 185
//			double base = N * (1 - Math.pow(CF, 1 / N));
//			if (e == 0) {
//				return base;
//			}
//
//			// Use linear interpolation between 0 and 1 like C4.5 does
//			return base + e * (addErrs(N, 1, CF) - base);
//		}
//
//		// Use linear interpolation at the high end (i.e. between N - 0.5
//		// and N) because of the continuity correction
//		if (e + 0.5 >= N) {
//
//			// Make sure that we never return anything smaller than zero
//			return Math.max(N - e, 0);
//		}
//
//		// Get z-score corresponding to CF
//		double z = normalInverse(1 - CF);
//
//		// Compute upper limit of confidence interval
//		double f = (e + 0.5) / N;
//		double r = (f + (z * z) / (2 * N) + z
//				* Math.sqrt((f / N) - (f * f / N) + (z * z / (4 * N * N))))
//				/ (1 + (z * z) / N);
//
//		return (r * N) - e;
//	}
//
//	protected static final double P0[] = { -5.99633501014107895267E1,
//			9.80010754185999661536E1, -5.66762857469070293439E1,
//			1.39312609387279679503E1, -1.23916583867381258016E0, };
//	protected static final double Q0[] = {
//	/* 1.00000000000000000000E0, */
//	1.95448858338141759834E0, 4.67627912898881538453E0,
//			8.63602421390890590575E1, -2.25462687854119370527E2,
//			2.00260212380060660359E2, -8.20372256168333339912E1,
//			1.59056225126211695515E1, -1.18331621121330003142E0, };
//
//	protected static final double P1[] = { 4.05544892305962419923E0,
//			3.15251094599893866154E1, 5.71628192246421288162E1,
//			4.40805073893200834700E1, 1.46849561928858024014E1,
//			2.18663306850790267539E0, -1.40256079171354495875E-1,
//			-3.50424626827848203418E-2, -8.57456785154685413611E-4, };
//	protected static final double Q1[] = {
//	/* 1.00000000000000000000E0, */
//	1.57799883256466749731E1, 4.53907635128879210584E1,
//			4.13172038254672030440E1, 1.50425385692907503408E1,
//			2.50464946208309415979E0, -1.42182922854787788574E-1,
//			-3.80806407691578277194E-2, -9.33259480895457427372E-4, };
//
//	protected static final double P2[] = { 3.23774891776946035970E0,
//			6.91522889068984211695E0, 3.93881025292474443415E0,
//			1.33303460815807542389E0, 2.01485389549179081538E-1,
//			1.23716634817820021358E-2, 3.01581553508235416007E-4,
//			2.65806974686737550832E-6, 6.23974539184983293730E-9, };
//	protected static final double Q2[] = {
//	/* 1.00000000000000000000E0, */
//	6.02427039364742014255E0, 3.67983563856160859403E0,
//			1.37702099489081330271E0, 2.16236993594496635890E-1,
//			1.34204006088543189037E-2, 3.28014464682127739104E-4,
//			2.89247864745380683936E-6, 6.79019408009981274425E-9, };
//
//	double normalInverse(double y0) {
//
//		double x, y, z, y2, x0, x1;
//		int code;
//
//		final double s2pi = Math.sqrt(2.0 * Math.PI);
//
//		if (y0 <= 0.0)
//			throw new IllegalArgumentException();
//		if (y0 >= 1.0)
//			throw new IllegalArgumentException();
//		code = 1;
//		y = y0;
//		if (y > (1.0 - 0.13533528323661269189)) { /* 0.135... = exp(-2) */
//			y = 1.0 - y;
//			code = 0;
//		}
//
//		if (y > 0.13533528323661269189) {
//			y = y - 0.5;
//			y2 = y * y;
//			x = y + y * (y2 * polevl(y2, P0, 4) / p1evl(y2, Q0, 8));
//			x = x * s2pi;
//			return (x);
//		}
//
//		x = Math.sqrt(-2.0 * Math.log(y));
//		x0 = x - Math.log(x) / x;
//
//		z = 1.0 / x;
//		if (x < 8.0) /* y > exp(-32) = 1.2664165549e-14 */
//			x1 = z * polevl(z, P1, 8) / p1evl(z, Q1, 8);
//		else
//			x1 = z * polevl(z, P2, 8) / p1evl(z, Q2, 8);
//		x = x0 - x1;
//		if (code != 0)
//			x = -x;
//		return (x);
//	}
//
//	public static double polevl(double x, double coef[], int N) {
//
//		double ans;
//		ans = coef[0];
//
//		for (int i = 1; i <= N; i++)
//			ans = ans * x + coef[i];
//
//		return ans;
//	}
//
//	public static double p1evl(double x, double coef[], int N) {
//
//		double ans;
//		ans = x + coef[0];
//
//		for (int i = 1; i < N; i++)
//			ans = ans * x + coef[i];
//
//		return ans;
//	}
//
//	// double pruneStep(DefaultMutableTreeNode node, int totalNum, int attrNum,
//	// double[] labels) {
//	// if (node.isLeaf()) {
//	// return computeCost(node, labels) + 1;
//	// }
//	// double minCost1 = pruneStep(
//	// (DefaultMutableTreeNode) node.getChildAt(0), totalNum, attrNum,
//	// labels);
//	// double minCost2 = pruneStep(
//	// (DefaultMutableTreeNode) node.getChildAt(1), totalNum, attrNum,
//	// labels);
//	// double costSplit = MyMath.log2(attrNum) + MyMath.log2(2 - 1);
//	// double c1 = computeCost(node, labels) + 1;
//	// double c2 = costSplit + 1 + minCost1 + minCost2;
//	// if (c1 < c2) {
//	// DefaultMutableTreeNode n = new DefaultMutableTreeNode();
//	// n.setUserObject(majorityLabel(node));
//	// node.removeAllChildren();
//	// node.add(n);
//	// return c1;
//	// }
//	// return c2;
//	// }
//
	int majorityLabel(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode leaf = node.getFirstLeaf();
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		while (leaf != null && leaf.isNodeAncestor(node)) {
			SplittingCriterion criterion = (SplittingCriterion) leaf
					.getUserObject();
			int key = criterion.attr;
			if (map.containsKey(key)) {
				int count = map.get(key);
				map.put(key, count + 1);
			} else {
				map.put(key, 1);
			}
			leaf = leaf.getNextLeaf();
		}
		int label = 0;
		int maxCount = 0;
		for (Map.Entry<Integer, Integer> e : map.entrySet()) {
			if (e.getValue() > maxCount) {
				maxCount = e.getValue();
				label = e.getKey();
			}
		}
		return label;
	}

	// double computeCost(DefaultMutableTreeNode node,double[] labels)
	// {
	// double cost=0;
	// node.getFirstLeaf()
	// return cost;
	// }

	void updateNode(TreeNode node, Boolean correct, int num) {
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
		// if (!n.isLeaf()) {
		SplittingCriterion criterion = (SplittingCriterion) n.getUserObject();
		if (correct)
			criterion.value += num;
		else
			criterion.value1 += num;
		// }
		TreeNode parent = n.getParent();
		if (parent != null)
			updateNode(parent, correct, num);
	}

	class ClassifyInfo {
		int label;
		TreeNode node;
	}

	ClassifyInfo classifyStep(DefaultMutableTreeNode node, Sample sample) {
		if (node.isLeaf()) {
			ClassifyInfo ci = new ClassifyInfo();
			SplittingCriterion criterion=(SplittingCriterion) node.getUserObject();
			ci.label = criterion.attr;
			ci.node = node;
			return ci;
		}
		SplittingCriterion criterion = (SplittingCriterion) node
				.getUserObject();
		return classifyStep((DefaultMutableTreeNode) node.getChildAt(criterion
				.getPosition(sample)), sample);
	}

	double computeEntropy(SampleSet dataset) {
		double entropy = 0;
		for (int i = 0; i < dataset.getNumOfLabels(); i++) {
			double p = ((double) dataset.getCount(i)) / dataset.getTotalCount();
			entropy -= p * MyMath.log2(p);
		}
		return entropy;
	}

	double computeEntropy(List<Sample> array) {
		Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < array.size(); i++) {
			int key = array.get(i).getLabel();
			if (countMap.containsKey(key)) {
				int count = countMap.get(key);
				count++;
				countMap.put(key, count);
			} else {
				countMap.put(key, 1);
			}
		}

		double entropy = 0;
		for (Map.Entry<Integer, Integer> e : countMap.entrySet()) {
			double p = ((double) e.getValue()) / array.size();
			entropy -= p * MyMath.log2(p);
		}
		return entropy;
	}

	// currently only for attribute that is not discrete
	List<SplittingCriterion> computeEntropy(SampleSet dataset, int attr) {
		List<Sample> sortedArray = dataset.getSortedListByAttribute(attr);

		SplittingCriterion maxResult = new SplittingCriterion();
		maxResult.value = Double.MIN_VALUE;
		if (sortedArray.size() == 1)
			sortedArray.add(sortedArray.get(0));
		List<SplittingCriterion> l = new ArrayList<SplittingCriterion>();
		for (int i = 1; i < sortedArray.size(); i++) {
			SplittingCriterion r = new SplittingCriterion();
			r.attr = attr;
			r.splitPoints = new double[1];
			r.splitPoints[0] = (sortedArray.get(i).getAttributes()[attr] + sortedArray
					.get(i - 1).getAttributes()[attr]) / 2;
			double entropy = (((double) i) / sortedArray.size())
					* computeEntropy(sortedArray.subList(0, i))
					+ (((double) (sortedArray.size() - i)) / sortedArray.size())
					* computeEntropy(sortedArray.subList(i, sortedArray.size()));
			double gain = computeEntropy(dataset) - entropy;
			double splitInfo = -((((double) i) / sortedArray.size()) * MyMath
					.log2(((double) i) / sortedArray.size()))
					- ((((double) (sortedArray.size() - i)) / sortedArray
							.size()) * MyMath.log2(((double) (sortedArray
							.size() - i)) / sortedArray.size()));
			r.value1 = gain;
			r.value = gain / splitInfo;
			l.add(r);
		}

		return l;
	}

	public static void main(String[] args) {
		int fold = 10;
		SampleParser sp = new SampleParser("train instance");
		Validator validator = new Validator(sp.parseForTest("train label"),
				fold);
		for (int k = 0; k < fold; k++) {
			System.out.println("validation " + k);
			long currentTime = System.currentTimeMillis();
			System.out.print("start");

			ClassificationAlgorithm classification = new DecisionTree(
					new DecisionTreeStrategyC45());
			SampleSet set = validator.nextSampleSet();
			set.setTestSamples(validator.getCurrentTestSet());
			List<Integer> attributeList = new ArrayList<Integer>();
			for (int i = 0; i < set.getData(0).get(0).getAttributes().length; i++) {
				attributeList.add(i);
			}
			classification.train(set, attributeList);
			System.out.println("complete timeused="
					+ (System.currentTimeMillis() - currentTime) + "ms");

			int faultCount = 0;
			List<Sample> test = validator.getCurrentTestSet();
			for (int i = 0; i < test.size(); i++) {
				int r = classification.classify(test.get(i));
				if (r != test.get(i).getLabel()) {
					faultCount++;
				}
			}
			System.out.println("falut percent = " + (double) faultCount
					/ test.size());
		}

	}

}
