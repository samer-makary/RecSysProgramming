package assignments.assignment5;

import org.grouplens.lenskit.basic.AbstractGlobalItemScorer;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import java.util.Collection;
import java.util.List;

/**
 * Global item scorer to find similar items.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleGlobalItemScorer extends AbstractGlobalItemScorer {
    private final SimpleItemItemModel model;

    @Inject
    public SimpleGlobalItemScorer(SimpleItemItemModel mod) {
        model = mod;
    }

    /**
     * Score items with respect to a set of reference items.
     * @param items The reference items.
     * @param scores The score vector. Its domain is the items to be scored, and the scores should
     *               be stored into this vector.
     */
    @Override
    public void globalScore(@Nonnull Collection<Long> items, @Nonnull MutableSparseVector scores) {
        scores.fill(0);
        // TODO score items in the domain of scores
        // each item's score is the sum of its similarity to each item in items, if they are
        // neighbors in the model.
        LongSet itemsSet = LongUtils.packedSet(items);
        
        for (VectorEntry ve : scores.fast()) {
        	long item = ve.getKey();
        	double itemScore = 0.0;
        	
        	// get the similarity vector of the item
        	List<ScoredId> neighbors = model.getNeighbors(item);
        	for (ScoredId s : neighbors) {
        		if (itemsSet.contains(s.getId())) {
        			itemScore += s.getScore();
        		}
        	}
        	
        	scores.set(item, itemScore);
        }
    }
}
