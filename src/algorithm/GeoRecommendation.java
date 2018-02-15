package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(double lat, double lon, String userId){
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection db = DBConnectionFactory.getDBConnection();

		// step 1 Get all favorited items
		Set<String> favoriteItems = db.getFavoriteItemIds(userId);
		
                             // step 2 Get all categories of favorited items, sort by count
		Map<String, Integer> allCategories = new HashMap<>(); // step 2
		for (String item : favoriteItems) {
			Set<String> categories = db.getCategories(item); // db queries
			for (String category : categories) {
				if (allCategories.containsKey(category)) {
					allCategories.put(category, allCategories.get(category) + 1);
				} else {
					allCategories.put(category, 1);
				}
			}
		}


		
		List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2){
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});
		Set<Item> visitedItems = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = db.searchItems(lat, lon, category.getKey());
			List<Item> tmp = new ArrayList<>();
			for (Item item: items) {
				if (!favoriteItems.contains(item.getItemId()) && !visitedItems.contains(item)) {
					tmp.add(item);
					
				}
			}
			
			Collections.sort(tmp, new Comparator<Item>() {
				public int compare(Item i1,Item i2){
					return Double.compare(i1.getDistance(), i2.getDistance());
				}
			});
			visitedItems.addAll(tmp);
			recommendedItems.addAll(tmp);
			
		}
		
		
		return recommendedItems;
	}
}
