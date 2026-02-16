package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private static final String DEFAULT_CONTENT_TYPE = "image/png";
    private static final String DATA_URL_PREFIX = "data:";
    private static final String BASE64_PREFIX = "base64,";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AddressRepository addressRepository;
    private final ContactRepository contactRepository;
    private final ItemDetailRepository itemDetailRepository;

    @Value("${app.upload.image-dir:uploads/images}")
    private String imageDir;

    @Value("${app.api.base-url:http://localhost:8081}")
    private String baseUrl;

    private String baseUrlNorm() {
        return baseUrl.trim().replaceAll("/$", "");
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> findAll() {
        return itemRepository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
                .map(i -> ItemResponse.fromEntity(i, baseUrlNorm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> findAllByUserId(Long userId) {
        return itemRepository.findByUser_IdAndActiveTrueOrderByCreatedAtDesc(userId).stream()
                .map(i -> ItemResponse.fromEntity(i, baseUrlNorm()))
                .toList();
    }

    /** Public: list active items by category. */
    @Transactional(readOnly = true)
    public List<ItemResponse> findByCategoryId(String categoryId) {
        return itemRepository.findByCategory_IdAndActiveTrueOrderByCreatedAtDesc(categoryId).stream()
                .map(i -> ItemResponse.fromEntity(i, baseUrlNorm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse findById(String id, Long userId) {
        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        return ItemResponse.fromEntity(item, baseUrlNorm());
    }

    @Transactional
    public ItemResponse create(CreateItemRequest request, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String itemId = UUID.randomUUID().toString();
        ImageSaveResult imageResult = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            imageResult = saveItemImage(user.getId(), itemId, request.getImageBase64(), request.getImageContentType());
        }

        Address address = null;
        if (request.getAddress() != null) {
            address = new Address();
            address.setAddressName(request.getAddress().getAddressName().trim());
            address.setLongitude(request.getAddress().getLongitude());
            address.setLatitude(request.getAddress().getLatitude());
            address = addressRepository.save(address);
        }

        Contact contact = null;
        if (request.getContact() != null) {
            contact = new Contact();
            contact.setFirstName(request.getContact().getFirstName().trim());
            contact.setLastName(request.getContact().getLastName().trim());
            contact.setPhone(request.getContact().getPhone() != null ? request.getContact().getPhone().trim() : null);
            contact = contactRepository.save(contact);
        }

        Item item = new Item();
        item.setId(itemId);
        item.setDescription(request.getDescription().trim());
        item.setUser(user);
        item.setCategory(category);
        item.setAddress(address);
        item.setContact(contact);
        item.setActive(request.getActive() != null ? request.getActive() : true);
        if (imageResult != null) {
            item.setImagePath(imageResult.relativePath());
            item.setImageContentType(imageResult.contentType());
        }
        item = itemRepository.save(item);

        if (request.getDetail() != null) {
            ItemDetail detail = new ItemDetail();
            detail.setItem(item);
            detail.setQuantity(request.getDetail().getQuantity() != null ? request.getDetail().getQuantity() : 1);
            detail.setPrice(request.getDetail().getPrice() != null ? request.getDetail().getPrice() : BigDecimal.ZERO);
            item.setDetail(detail);
            itemRepository.save(item);
        }

        return ItemResponse.fromEntity(item, baseUrlNorm());
    }

    @Transactional
    public ItemResponse update(String id, UpdateItemRequest request, Long userId) throws IOException {
        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        item.setDescription(request.getDescription().trim());
        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }

        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            if (item.getImagePath() != null) {
                deleteItemImage(item);
            }
            ImageSaveResult result = saveItemImage(item.getUser().getId(), item.getId(), request.getImageBase64(), request.getImageContentType());
            itemRepository.updateImagePathAndContentType(item.getId(), result.relativePath(), result.contentType());
            item.setImagePath(result.relativePath());
            item.setImageContentType(result.contentType());
        }

        if (request.getAddress() != null) {
            if (item.getAddress() != null) {
                Address a = item.getAddress();
                a.setAddressName(request.getAddress().getAddressName().trim());
                a.setLongitude(request.getAddress().getLongitude());
                a.setLatitude(request.getAddress().getLatitude());
                addressRepository.save(a);
            } else {
                Address address = new Address();
                address.setAddressName(request.getAddress().getAddressName().trim());
                address.setLongitude(request.getAddress().getLongitude());
                address.setLatitude(request.getAddress().getLatitude());
                address = addressRepository.save(address);
                item.setAddress(address);
            }
        }

        if (request.getContact() != null) {
            if (item.getContact() != null) {
                Contact c = item.getContact();
                c.setFirstName(request.getContact().getFirstName().trim());
                c.setLastName(request.getContact().getLastName().trim());
                c.setPhone(request.getContact().getPhone() != null ? request.getContact().getPhone().trim() : null);
                contactRepository.save(c);
            } else {
                Contact contact = new Contact();
                contact.setFirstName(request.getContact().getFirstName().trim());
                contact.setLastName(request.getContact().getLastName().trim());
                contact.setPhone(request.getContact().getPhone() != null ? request.getContact().getPhone().trim() : null);
                contact = contactRepository.save(contact);
                item.setContact(contact);
            }
        }

        if (request.getDetail() != null) {
            ItemDetail detail = item.getDetail();
            if (detail == null) {
                detail = new ItemDetail();
                detail.setItem(item);
                item.setDetail(detail);
            }
            detail.setQuantity(request.getDetail().getQuantity() != null ? request.getDetail().getQuantity() : 1);
            detail.setPrice(request.getDetail().getPrice() != null ? request.getDetail().getPrice() : BigDecimal.ZERO);
            itemDetailRepository.save(detail);
        }

        return ItemResponse.fromEntity(item, baseUrlNorm());
    }

    @Transactional
    public void delete(String id, Long userId) throws IOException {
        Item item = itemRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (item.getImagePath() != null) {
            deleteItemImage(item);
        }
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> findAllForAdmin() {
        return itemRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(i -> ItemResponse.fromEntity(i, baseUrlNorm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Item findByIdPublic(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    public Path getItemImagePath(Item item) {
        if (item.getImagePath() == null || item.getImagePath().isBlank()) {
            return null;
        }
        Path base = Path.of(imageDir);
        if (!base.isAbsolute()) {
            base = Path.of(System.getProperty("user.dir")).resolve(base);
        }
        return base.resolve("items").resolve(item.getImagePath());
    }

    private ImageSaveResult saveItemImage(Long userId, String itemId, String base64Data, String requestContentType) throws IOException {
        String contentType = resolveContentType(base64Data, requestContentType);
        if (base64Data.startsWith(DATA_URL_PREFIX)) {
            int idx = base64Data.indexOf(BASE64_PREFIX);
            if (idx >= 0) {
                base64Data = base64Data.substring(idx + BASE64_PREFIX.length());
            }
        }
        byte[] bytes = Base64.getDecoder().decode(base64Data.trim());
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Invalid or empty Base64 image data");
        }
        Path base = Path.of(imageDir);
        if (!base.isAbsolute()) {
            base = Path.of(System.getProperty("user.dir")).resolve(base);
        }
        Path itemDir = base.resolve("items").resolve(String.valueOf(userId));
        Files.createDirectories(itemDir);
        String relativePath = userId + "/" + itemId;
        Path filePath = itemDir.resolve(itemId);
        Files.write(filePath, bytes);
        return new ImageSaveResult(relativePath, contentType);
    }

    private record ImageSaveResult(String relativePath, String contentType) {}

    private void deleteItemImage(Item item) throws IOException {
        Path imagePath = getItemImagePath(item);
        if (imagePath != null && Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
    }

    private String resolveContentType(String base64Data, String requestContentType) {
        if (requestContentType != null && !requestContentType.isBlank()) {
            return requestContentType.trim();
        }
        if (base64Data.startsWith(DATA_URL_PREFIX)) {
            int end = base64Data.indexOf(';');
            if (end > DATA_URL_PREFIX.length()) {
                return base64Data.substring(DATA_URL_PREFIX.length(), end).trim();
            }
        }
        return DEFAULT_CONTENT_TYPE;
    }
}
