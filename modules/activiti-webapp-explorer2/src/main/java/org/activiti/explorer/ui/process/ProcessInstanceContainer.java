package org.activiti.explorer.ui.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.explorer.data.LazyLoadingContainer;
import org.activiti.explorer.data.LazyLoadingQuery;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

import static org.activiti.explorer.ui.process.ProcessInstanceItem.*;

/**
 * 
 * @author Michel Daviot
 *
 */
public class ProcessInstanceContainer implements Container.Hierarchical {
	private static final String PREFIX_DEFINITION = "DEF_";
	private static final long serialVersionUID = 1L;
	private final LazyLoadingQuery lazyLoadingQuery;
	protected Map<Object, Item> itemCache = new HashMap<Object, Item>();
	protected List<Object> containerPropertyIds = new ArrayList<Object>();
    protected Map<Object, Class<?>> containerPropertyTypes = new HashMap<Object, Class<?>>();
    protected Map<Object, Object> containerPropertyDefaultValues = new HashMap<Object, Object>();

	public ProcessInstanceContainer(LazyLoadingQuery lazyLoadingQuery) {
		this.lazyLoadingQuery = lazyLoadingQuery;
	}
	 
	@Override
	public Item getItem(Object itemId) {
		if (isRootDefinition(itemId)){//special case to handle roots as definitions
			ProcessInstanceItem item = new ProcessInstanceItem();
			item.addItemProperty(PROPERTY_NAME, new ObjectProperty<String>(itemId.toString().substring(PREFIX_DEFINITION.length()), String.class));
			return item;
		} else {
			return itemCache.get(itemId);
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
		List<Object> items = new ArrayList<Object>();
		if(isRootDefinition(itemId)) {// root : definition
			String definition = getDefinition(itemId);
			for (Entry<Object, Item> entry : itemCache.entrySet()) {
				Item item =entry.getValue();
				if(definition.equals(item.getItemProperty(PROPERTY_DEFINITION).getValue())){
					items.add(entry.getKey());
				}
			}
		}
		else {
			for (Entry<Object, Item> entry : itemCache.entrySet()) {
				Item item =entry.getValue();
				if(itemId.equals(item.getItemProperty(PROPERTY_SUPER_ID).getValue())){
					items.add(entry.getKey());
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
	
	@Override
	public boolean containsId(Object itemId) {
		return itemCache.containsKey(itemId) || itemCache.containsValue(itemId);
	}

	private void updateCache() {
		if (itemCache.isEmpty()) {
			List<Item> batch = lazyLoadingQuery.loadItems(0, lazyLoadingQuery.size());
			for (Item batchItem : batch) {
				itemCache.put(batchItem.getItemProperty(PROPERTY_ID).getValue(), batchItem);
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

	 public Object addItem() throws UnsupportedOperationException {
		    throw new UnsupportedOperationException();
		  }
		  
	public Object addItemAfter(Object previousItemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Item addItemAfter(Object previousItemId, Object newItemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Object addItemAt(int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Item addItemAt(int index, Object newItemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	
	 public Collection< ? > getContainerPropertyIds() {
		    return containerPropertyIds;
		  }

		

	@Override
	public Collection<?> getItemIds() {
		return itemCache.keySet();
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		return getItem(itemId).getItemProperty(propertyId);
	}


	public Class<?> getType(Object propertyId) {
		return containerPropertyTypes.get(propertyId);
	}

	public Object getDefaultValue(Object propertyId) {
		return containerPropertyDefaultValues.get(propertyId);
	}

	@Override
	public int size() {
		return itemCache.size();
	}

	public boolean addContainerProperty(Object propertyId, Class< ? > type, Object defaultValue) throws UnsupportedOperationException {
	    containerPropertyIds.add(propertyId);
	    containerPropertyTypes.put(propertyId, type);
	    containerPropertyDefaultValues.put(propertyId, defaultValue);
	    return true;
	  }

	  public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
	    containerPropertyIds.remove(propertyId);
	    containerPropertyTypes.remove(propertyId);
	    containerPropertyDefaultValues.remove(propertyId);
	    return true;
	  }
	  

	public boolean removeAllItems() throws UnsupportedOperationException {
		itemCache.clear();
		return true;
	}

}
