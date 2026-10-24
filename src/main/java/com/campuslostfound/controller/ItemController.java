
package com.campuslostfound.controller;

import com.campuslostfound.entity.Item;
import com.campuslostfound.entity.User;
import com.campuslostfound.repository.UserRepository;
import com.campuslostfound.service.ItemService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public ItemController(
            ItemService itemService,
            UserRepository userRepository,
            Cloudinary cloudinary) {
        this.itemService = itemService;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    // Add an item and record its poster
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> addItem(
            @RequestPart("item") Item item,
            @RequestPart(value = "image", required = false)
            MultipartFile image,
            Authentication authentication) {

        try {
            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401)
                        .body(Map.of("message",
                                "Please log in first."));
            }

            String email = authentication.getName();

            User poster = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Logged-in user was not found."));

            item.setPostedBy(poster);

            if (image != null && !image.isEmpty()) {

                String contentType = image.getContentType();

                if (contentType == null ||
                        !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message",
                                    "Please upload an image file."));
                }

                // Limit uploads to 5 MB
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message",
                                    "Image must be smaller than 5 MB."));
                }

                // Upload the image to Cloudinary
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "image",
                                "folder", "campus-lost-found"
                        )
                );

                String imageUrl =
                        (String) uploadResult.get("secure_url");

                if (imageUrl == null || imageUrl.isBlank()) {
                    return ResponseEntity.internalServerError()
                            .body(Map.of("message",
                                    "Image upload did not return a URL."));
                }

                // Save the permanent image URL in MySQL
                item.setImagePath(imageUrl);
            }

            Item savedItem = itemService.addItem(item);
            return ResponseEntity.ok(savedItem);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message",
                            "Unable to read the uploaded image."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message",
                            "Unable to upload the image or save the item."));
        }
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(
            @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(
            @PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok("Item deleted successfully");
    }

    @GetMapping("/search/category")
    public ResponseEntity<List<Item>> searchByCategory(
            @RequestParam String category) {
        return ResponseEntity.ok(
                itemService.searchByCategory(category));
    }

    @GetMapping("/search/location")
    public ResponseEntity<List<Item>> searchByLocation(
            @RequestParam String location) {
        return ResponseEntity.ok(
                itemService.searchByLocation(location));
    }

    @GetMapping("/search/type")
    public ResponseEntity<List<Item>> searchByType(
            @RequestParam String type) {
        return ResponseEntity.ok(
                itemService.searchByType(type));
    }
}
