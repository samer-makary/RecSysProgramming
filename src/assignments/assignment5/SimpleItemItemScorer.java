package assignments.assignment5;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleItemItemScorer extends AbstractItemScorer {
    private final SimpleItemItemModel model;
    private final UserEventDAO userEvents;
    private final int neighborhoodSize;

    @Inject
    public SimpleItemItemScorer(SimpleItemItemModel m, UserEventDAO dao,
                                @NeighborhoodSize int nnbrs) {
        model = m;
        userEvents = dao;
        neighborhoodSize = nnbrs;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param scores The score vector.  Its key domain is the items to score, and the scores
     *               (rating predictions) should be written back to this vector.
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
    	SparseVector ratings = getUserRatingVector(user);

        for (VectorEntry e: scores.fast(VectorEntry.State.EITHER)) {
            long item = e.getKey();
            List<ScoredId> neighbors = model.getNeighbors(item);
            // TODO Score this item and save the score into scores
            int neighborhood = 0;
            double sumWeightedSims = 0.0;
            double sumAbsSims = 0.0;
            
            // get an iterator on the list of items in the neighborhood of 
            // the item that we want to score
            Iterator<ScoredId> scoreItr = neighbors.iterator();
            while (scoreItr.hasNext() && neighborhood < neighborhoodSize) {
            	ScoredId score = scoreItr.next();
            	long otherItem = score.getId();
            	double simItemOtherItem = score.getScore();
            	
            	// check to see if the user has rated that other item
            	if (ratings.containsKey(otherItem)) {
            		neighborhood++;
            		sumWeightedSims += (simItemOtherItem * ratings.get(otherItem));
            		sumAbsSims += Math.abs(simItemOtherItem);
            	}
            }
            
            // compute the overall score the item
            scores.set(item, sumWeightedSims / sumAbsSims); 
        }
    }

    /**
     * Get a user's ratings.
     * @param user The user ID.
     * @return The ratings to retrieve.
     */
    private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userEvents.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }

        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }
}
