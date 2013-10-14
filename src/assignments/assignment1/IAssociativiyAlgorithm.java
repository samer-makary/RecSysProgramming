package assignments.assignment1;

import org.grouplens.lenskit.vectors.MutableSparseVector;

public interface IAssociativiyAlgorithm {

	/**
	 * The function computes the associativity score by which every item in
	 * associated with the item X.
	 * 
	 * @param dataModel
	 *            The data model that includes the raters of every item.
	 * @param itemX
	 *            The item which we need to find other items associated with it.
	 * @param associatedWithX
	 *            A vector whose keys are the items candidate to be associated
	 *            with X. After the function finishes computation the values
	 *            corresponding to each key will be either the required score or
	 *            {@code Double#NaN} in case the algorithm failed to compute the
	 *            associativity score
	 */
	public void scoreItems(NonPersonalizedDataModel dataModel, long itemX,
			MutableSparseVector associatedWithX);
}
