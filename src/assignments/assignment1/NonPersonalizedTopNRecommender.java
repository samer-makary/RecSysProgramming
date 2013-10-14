package assignments.assignment1;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collections;
import java.util.List;

import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

public class NonPersonalizedTopNRecommender {
	
	protected final GlobalItemScorer scorer;

    public NonPersonalizedTopNRecommender(GlobalItemScorer scorer) {
        this.scorer = scorer;
    }

    protected List<ScoredId> globalRecommend(long itemX, int n, LongSet candidates) {
    	LongSet itemXSet = LongUtils.packedSet(itemX);
        SparseVector scores = scorer.globalScore(itemXSet, candidates);
        return recommend(n, scores);
    }

    private List<ScoredId> recommend(int n, SparseVector scores) {
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        if (n < 0) {
            n = scores.size();
        }

        ScoredItemAccumulator accum = new TopNScoredItemAccumulator(n);
        for (VectorEntry pred : scores.fast()) {
            final double v = pred.getValue();
            accum.put(pred.getKey(), v);
        }

        return accum.finish();
    }
}
