
package com.campuslostfound.service;

import com.campuslostfound.entity.Item;
import com.campuslostfound.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item addItem(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Item not found"));
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    // Search items by category
    public List<Item> searchByCategory(String category) {
        return itemRepository.findByCategoryContainingIgnoreCase(category);
    }

    // Search items by location
    public List<Item> searchByLocation(String location) {
        return itemRepository.findByLocationContainingIgnoreCase(location);
    }

    // Filter items by LOST or FOUND
    public List<Item> searchByType(String type) {
        return itemRepository.findByTypeIgnoreCase(type);
    }

}
