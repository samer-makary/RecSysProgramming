package assignments.assignment1;

import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class AdvancedAssociativity implements IAssociativiyAlgorithm {

	@Override
	public void scoreItems(NonPersonalizedDataModel dataModel, long itemX,
			MutableSparseVector associatedWithX) {

		// Pull the raters set for item X
		LongSortedSet xRaters = dataModel.getRatersOfItem(itemX);
		
		// Get all candidate items
		LongSortedSet cands = associatedWithX.keyDomain();
		LongSortedSet notXRaters = getNotXRaters(itemX, dataModel, cands);
		for (long itemY : cands) {
			// Pull the raters set for item Y
			LongSortedSet yRaters = dataModel.getRatersOfItem(itemY);
			double score = computeSimpleAssociation(xRaters, yRaters)
					/ computeSimpleAssociation(notXRaters, yRaters);
			associatedWithX.set(itemY, score);
		}
	}
	
	private double computeSimpleAssociation(LongSortedSet a, LongSortedSet b) {
		// Remember from Set Theory, OR:Union, AND:Intersection
		// A AND B = (A OR B) - (A - B) - (B - A)
		LongSortedSet xyIntersection = LongUtils.setDifference(LongUtils
				.setDifference(LongUtils.setUnion(a, b),
						LongUtils.setDifference(a, b)), LongUtils
				.setDifference(b, a));
		return 1.0 * xyIntersection.size() / a.size();
	}
	
	private LongSortedSet getNotXRaters(long itemX,
			NonPersonalizedDataModel dataModel, LongSortedSet cands) {
		LongSortedSet xRaters = dataModel.getRatersOfItem(itemX);
		LongSortedSet allRaters = null;
		for(long item : cands) {
				if (allRaters == null)
					allRaters = dataModel.getRatersOfItem(item);
				else
					allRaters = LongUtils.setUnion(allRaters, dataModel.getRatersOfItem(item));
		}
		
		return LongUtils.setDifference(allRaters, xRaters);
	}

}
