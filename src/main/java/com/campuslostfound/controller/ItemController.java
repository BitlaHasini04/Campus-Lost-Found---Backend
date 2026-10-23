
package com.campuslostfound.controller;

import com.campuslostfound.entity.Item;
import com.campuslostfound.entity.User;
import com.campuslostfound.repository.UserRepository;
import com.campuslostfound.service.ItemService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public ItemController(
            ItemService itemService,
            UserRepository userRepository) {
        this.itemService = itemService;
        this.userRepository = userRepository;
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
                        .body(Map.of("message", "Please log in first."));
            }

            String email = authentication.getName();

            User poster = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Logged-in user was not found."));

            // Save the logged-in user as the item poster
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

                Files.createDirectories(uploadDirectory);

                String originalName = image.getOriginalFilename();
                String extension = "";

                if (originalName != null &&
                        originalName.lastIndexOf(".") >= 0) {
                    extension = originalName.substring(
                            originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID() + extension;
                Path filePath = uploadDirectory.resolve(fileName)
                        .normalize();

                if (!filePath.startsWith(uploadDirectory)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Invalid file name."));
                }

                image.transferTo(filePath);
                item.setImagePath("/uploads/" + fileName);
            }

            Item savedItem = itemService.addItem(item);
            return ResponseEntity.ok(savedItem);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message",
                            "Unable to save the item image."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
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
