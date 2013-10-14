package assignments.assignment3;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

/**
 * User-user item scorer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleUserUserItemScorer extends AbstractItemScorer {
    // the following fields will be used to find the user neighborhood
    // and compute the rating (scoring) equation between a user and an item
    private final IRatingEquation ratingEqu;
    private final UserDAO userDAO;
    private final UserEventDAO userEventDAO;
    private final VectorSimilarity vecSim;
    private final int N;

	@Inject
	public SimpleUserUserItemScorer(
			UserDAO userDAO,
			UserEventDAO userEventDAO,
			@RatingEquationType IRatingEquation ratingEquation,
			@UserUserSimilarityMeasureType VectorSimilarity uuSimilarityMeasure,
			@NeighborhoodSize Integer neighborhoodSize) {
		
		this.userDAO = userDAO;
		this.userEventDAO = userEventDAO;
		ratingEqu = ratingEquation;
		vecSim = uuSimilarityMeasure;
		N = neighborhoodSize;
	}

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
    	
    	UserUserCollaborativeFilteringNeighbors neighSelector = 
    			new UserUserCollaborativeFilteringNeighbors(vecSim, userDAO, userEventDAO);
    	
        // This is the loop structure to iterate over items to score
        for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {

        	// find the neighborhood of the user given the current item
        	Long2DoubleMap neighbors = neighSelector.getUserNNeighbors(user, e.getKey(), N);
        	
        	// find and set the rating score for this item
        	double score = ratingEqu.rate(user, e.getKey(), neighbors);
        	scores.set(e.getKey(), score);
        }
    }

    /*
     * I moved the function getUserRatings to the rating equation class
     */
    
}
