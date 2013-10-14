package assignments.assignment1;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.List;

import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.data.DataSource;

public class NonPersonalizedDataModel {
	
	/**
	 * Map of Item ID to set of Users who rated this Item
	 */
	private Long2ObjectMap<LongSortedSet> itemRaters;
	
	/**
	 * Set of all the Items in the system 
	 */
	private LongSet items;
	
	public NonPersonalizedDataModel(LongSet items, Long2ObjectMap<LongSortedSet> itemRaters) {
		this.items = items;
		this.itemRaters = itemRaters;
	}
	
	public LongSet getItems() {
		return this.items;
	}
	
	public static NonPersonalizedDataModel createDataModel(DataSource dataSource) {
		NonPersonalizedDataModel model = null;
		
		// Get all Items IDs
		LongSet itemIDs = dataSource.getItemDAO().getItemIds();
		
		// Get the raters for every Item
		ItemEventDAO ieDAO = dataSource.getItemEventDAO();
		Long2ObjectMap<LongSortedSet> ratersMap;
		ratersMap = new Long2ObjectArrayMap<LongSortedSet>(itemIDs.size());
		LongIterator itemsItr = itemIDs.iterator();
		while (itemsItr.hasNext()) {
			// Find the raters for every Item
			long item = itemsItr.nextLong();
			List<Rating> ratingsList = ieDAO.getEventsForItem(item, Rating.class);
			LongSortedSet lss = new LongAVLTreeSet();
			for (Rating r : ratingsList)
				lss.add(r.getUserId());
			ratersMap.put(item, lss);
			
		}
		
		model = new NonPersonalizedDataModel(itemIDs, ratersMap);
		
		return model;
	}
	
	public LongSortedSet getRatersOfItem(long itemID) {
		if (this.items.contains(itemID))
			return this.itemRaters.get(itemID);
		else
			return null;
	}
}
