package assignments.assignment1;

import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class SimpleAssociativity implements IAssociativiyAlgorithm {

	@Override
	public void scoreItems(NonPersonalizedDataModel dataModel, long itemX,
			MutableSparseVector associatedWithX) {
		
		// Pull the raters set for item X
		LongSortedSet xRaters = dataModel.getRatersOfItem(itemX);
		
		// Get all candidate items
		LongSortedSet cands = associatedWithX.keyDomain();
		for (long itemY : cands) {
			// Pull the raters set for item Y
			LongSortedSet yRaters = dataModel.getRatersOfItem(itemY);
			double score = computeAssociativity(xRaters, yRaters);
			associatedWithX.set(itemY, score);
		}
		
	}

	private double computeAssociativity(LongSortedSet xSet, LongSortedSet ySet) {
		// Remember from Set Theory, OR:Union, AND:Intersection
		// A AND B = (A OR B) - (A - B) - (B - A)
		LongSortedSet xyIntersection = LongUtils.setDifference(LongUtils
				.setDifference(LongUtils.setUnion(xSet, ySet),
						LongUtils.setDifference(xSet, ySet)), LongUtils
				.setDifference(ySet, xSet));
		return 1.0 * xyIntersection.size() / xSet.size();
	}

}
