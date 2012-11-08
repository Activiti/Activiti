package org.activiti.explorer.ui.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;

import static org.activiti.explorer.ui.process.ProcessInstanceItem.*;

/**
 * 
 * @author Michel Daviot
 *
 */
public class ProcessInstanceContainer extends LazyLoadingContainer
		implements Container.Hierarchical {
	private static final String PREFIX_DEFINITION = "DEF_";
	private static final long serialVersionUID = 1L;

	public ProcessInstanceContainer(LazyLoadingQuery lazyLoadingQuery,
			int batchSize) {
		super(lazyLoadingQuery, batchSize);
	}

	public ProcessInstanceContainer(LazyLoadingQuery lazyLoadingQuery) {
		this(lazyLoadingQuery, 15);
	}
	 
	@Override
	public Item getItem(Object itemId) {
		if (isRootDefinition(itemId)){//special case to handle roots as definitions
			ProcessInstanceItem item = new ProcessInstanceItem();
			item.addItemProperty(PROPERTY_NAME, new ObjectProperty<String>(itemId.toString().substring(PREFIX_DEFINITION.length()), String.class));
			return item;
		} else {
			return super.getItem(itemId);
		}
	}
	
	private boolean isRootDefinition(Object itemId){
		return itemId.toString().startsWith(PREFIX_DEFINITION);
	}
	
	private String getDefinition(Object itemId){
		return itemId.toString().substring(PREFIX_DEFINITION.length());
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		List<Item> items = new ArrayList<Item>();
		if(isRootDefinition(itemId)) {// root : definition
			String definition = getDefinition(itemId);
			for (Item item : itemCache.values()) {
				if(definition.equals(item.getItemProperty(PROPERTY_DEFINITION).getValue())){
					items.add(item);
				}
			}
		}
		else {
			for (Item item : itemCache.values()) {
				if(itemId.equals(item.getItemProperty(PROPERTY_SUPER_ID))){
					items.add(item);
				}
			}
		}
		return items;
	}

	@Override
	public Object getParent(Object itemId) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public Collection<?> rootItemIds() {
		updateCache();
		Set<String> roots = new HashSet<String>();
		for (Entry<Object, Item> entry : itemCache.entrySet()) {
			roots.add(PREFIX_DEFINITION+entry.getValue().getItemProperty(PROPERTY_DEFINITION).getValue().toString());
		}
		return roots;
	}

	private void updateCache() {
		if (itemCache.isEmpty()) {
			List<Item> batch = lazyLoadingQuery.loadItems(0, lazyLoadingQuery.size());
			for (Item batchItem : batch) {
				itemCache.put(batchItem.getItemProperty(PROPERTY_ID), batchItem);
			}
		}
	}

	@Override
	public boolean setParent(Object itemId, Object newParentId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean areChildrenAllowed(Object itemId) {
		return true;
	}

	@Override
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public boolean isRoot(Object itemId) {
		return isRootDefinition(itemId);
	}

	@Override
	public boolean hasChildren(Object itemId) {
		return getChildren(itemId).size() > 0;
	}

}
