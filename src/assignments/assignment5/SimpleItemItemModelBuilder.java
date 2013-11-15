package assignments.assignment5;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleItemItemModelBuilder implements Provider<SimpleItemItemModel> {
    private final ItemDAO itemDao;
    private final UserEventDAO userEventDao;
//    private static final Logger logger = LoggerFactory.getLogger(SimpleItemItemModelBuilder.class);;

    @Inject
    public SimpleItemItemModelBuilder(@Transient ItemDAO idao,
                                      @Transient UserEventDAO uedao) {
        itemDao = idao;
        userEventDao = uedao;
    }

    @Override
    public SimpleItemItemModel get() {
        // Get the transposed rating matrix
        // This gives us a map of item IDs to those items' rating vectors
        Map<Long, ImmutableSparseVector> itemVectors = getItemVectors();

        // Get all items - you might find this useful
        LongSortedSet items = LongUtils.packedSet(itemVectors.keySet());

        // TODO Compute the similarities between each pair of items
        // Cosine Similarity object
//        CosineVectorSimilarity cosineSim = new CosineVectorSimilarity();
        
        // the following map will hold the accumulator of similarities for each item
        // it will then be converted to a MutableSparseVector
        Long2ObjectMap<ScoredItemAccumulator> itemSortedSimMap = 
        		new Long2ObjectArrayMap<ScoredItemAccumulator>(items.size());  
        for (long item : items)
        	itemSortedSimMap.put(item, new UnlimitedScoredItemAccumulator());
        
        // compute pair-wise similarity between items
        LongIterator itr1 = items.iterator();
        while (itr1.hasNext()) {
        	long item1 = itr1.nextLong();
        	
        	// get the set of users who rated item1
        	LongSortedSet usersRatedItem1 = itemVectors.get(item1).keySet();
        	LongIterator itr2 = items.iterator(item1);
        	while (itr2.hasNext()) {
        		long item2 = itr2.nextLong();
        		
        		// get the set of users who rated item2
        		LongSortedSet usersRatedItem2 = itemVectors.get(item2).keySet();
        		
        		// get the set of users who rated both items
        		// Remember from Set Theory, OR:Union, AND:Intersection
        		// A AND B = (A OR B) - (A - B) - (B - A)
        		LongSortedSet usersRatedItem1Item2 = LongUtils.setDifference(LongUtils
        				.setDifference(LongUtils.setUnion(usersRatedItem1, usersRatedItem2),
        						LongUtils.setDifference(usersRatedItem1, usersRatedItem2)), LongUtils
        				.setDifference(usersRatedItem2, usersRatedItem1));
        		
        		// now if item1 and item2 have users ratings in common, then compute the similarity between them
        		if (!usersRatedItem1Item2.isEmpty()) {
        			double simItem1Item2 = 0.0;
        			for (long u : usersRatedItem1Item2) {
            			simItem1Item2 += (itemVectors.get(item1).get(u) * itemVectors.get(item2).get(u));
            		}
            		
        			simItem1Item2 /= (itemVectors.get(item1).norm() * itemVectors.get(item2).norm());
            		// keep only positive similarities (> 0)
                	if (Double.compare(simItem1Item2, 0) > 0) {
                		itemSortedSimMap.get(item1).put(item2, simItem1Item2);
                    	itemSortedSimMap.get(item2).put(item1, simItem1Item2);
                	}
        		}
        	}
        	
        }
        
        // convert accumulated scores
        Long2ObjectMap<List<ScoredId>> itemSimilarities = new Long2ObjectArrayMap<List<ScoredId>>(items.size());
        for (long item : items) {
        	itemSimilarities.put(item, itemSortedSimMap.get(item).finish());
        }
        
        // It will need to be in a map of longs to lists of Scored IDs to store in the model
        return new SimpleItemItemModel(itemSimilarities);
    }

    /**
     * Load the data into memory, indexed by item.
     * @return A map from item IDs to item rating vectors. Each vector contains users' ratings for
     * the item, keyed by user ID.
     */
    public Map<Long,ImmutableSparseVector> getItemVectors() {
        // set up storage for building each item's rating vector
        LongSet items = itemDao.getItemIds();
        // map items to maps from users to ratings
        Map<Long,Map<Long,Double>> itemData = new HashMap<Long, Map<Long, Double>>();
        for (long item: items) {
            itemData.put(item, new HashMap<Long, Double>());
        }
        // itemData should now contain a map to accumulate the ratings of each item

        // stream over all user events
        Cursor<UserHistory<Event>> stream = userEventDao.streamEventsByUser();
        try {
            for (UserHistory<Event> evt: stream) {
                MutableSparseVector vector = RatingVectorUserHistorySummarizer.makeRatingVector(evt).mutableCopy();
                // vector is now the user's rating vector
                
                // TODO Normalize this vector and store the ratings in the item data
                long userID = evt.getUserId();
                // normalize ratings vector by subtracting the mean of the user ratings
                double userRatingsMean = vector.mean();
                vector.add(-1.0 * userRatingsMean);
                
                // for every item the user rated, add an entry of (user, rating) in the item map
                for (VectorEntry ve : vector.fast()) { 
                	itemData.get(ve.getKey()).put(userID, ve.getValue());
                }
                
            }
        } finally {
            stream.close();
        }

        // This loop converts our temporary item storage to a map of item vectors
        Map<Long,ImmutableSparseVector> itemVectors = new HashMap<Long, ImmutableSparseVector>();
        for (Map.Entry<Long,Map<Long,Double>> entry: itemData.entrySet()) {
            MutableSparseVector vec = MutableSparseVector.create(entry.getValue());
            itemVectors.put(entry.getKey(), vec.immutable());
        }
        return itemVectors;
    }
}
