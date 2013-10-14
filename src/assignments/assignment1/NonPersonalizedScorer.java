package assignments.assignment1;

import java.util.Collection;

import org.grouplens.lenskit.basic.AbstractGlobalItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class NonPersonalizedScorer extends AbstractGlobalItemScorer {
	
	private IAssociativiyAlgorithm algorithm;
	private NonPersonalizedDataModel dataModel;
	
	public NonPersonalizedScorer(IAssociativiyAlgorithm algorithm,
			NonPersonalizedDataModel dataModel) {
		
		this.algorithm = algorithm;
		this.dataModel = dataModel;
	}

	@Override
	public void globalScore(Collection<Long> itemX, MutableSparseVector associativityScoreWithX) {
		if (itemX.size() != 1)
			throw new UnsupportedOperationException("The parameter <itemX> must contain only 1 item "
					+ "to find the associativity of the other elements with it.");
		
		// Clear any garbage score associated with any item 
		associativityScoreWithX.clear();
		
		// Run associativity score algorithm
		this.algorithm.scoreItems(this.dataModel, itemX.iterator().next(), associativityScoreWithX);
		
		// Check to make sure not compute associativity of X with itself
		long itemXID = itemX.iterator().next();
		if (associativityScoreWithX.containsKey(itemXID))
			associativityScoreWithX.unset(itemXID);

	}

}
