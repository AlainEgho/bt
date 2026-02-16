package com.example.backend.service;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.dto.CreateCategoryRequest;
import com.example.backend.dto.UpdateCategoryRequest;
import com.example.backend.entity.Category;
import com.example.backend.entity.User;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private static final Long USER_ID = 1L;
    private User user;
    private Category category;
    private String categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID().toString();
        user = new User();
        user.setId(USER_ID);
        user.setEmail("user@example.com");

        category = new Category();
        category.setId(categoryId);
        category.setDescription("Test Category");
        category.setUser(user);
        category.setActive(true);
        category.setCreatedAt(Instant.now());

        ReflectionTestUtils.setField(categoryService, "baseUrl", "http://localhost:8081");
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("returns list of CategoryResponse for user")
        void returnsList() {
            when(categoryRepository.findByUser_IdAndActiveTrueOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of(category));

            List<CategoryResponse> result = categoryService.findAllByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(categoryId);
            assertThat(result.get(0).getDescription()).isEqualTo("Test Category");
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("throws when category not found")
        void notFound_throws() {
            String unknownId = UUID.randomUUID().toString();
            when(categoryRepository.findByIdAndUser_Id(unknownId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(unknownId, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category not found");
        }

        @Test
        @DisplayName("returns CategoryResponse when owner matches")
        void success() {
            when(categoryRepository.findByIdAndUser_Id(categoryId, USER_ID))
                    .thenReturn(Optional.of(category));

            CategoryResponse result = categoryService.findById(categoryId, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(categoryId);
            assertThat(result.getDescription()).isEqualTo("Test Category");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("throws when user not found")
        void userNotFound_throws() {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setDescription("New Category");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.create(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("saves category and returns response")
        void success() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setDescription("  New Category  ");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            CategoryResponse result = categoryService.create(request, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("New Category");
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when category not found")
        void notFound_throws() {
            String unknownId = UUID.randomUUID().toString();
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setDescription("Updated");
            when(categoryRepository.findByIdAndUser_Id(unknownId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(unknownId, request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category not found");
        }

        @Test
        @DisplayName("updates description and saves")
        void success() throws Exception {
            UpdateCategoryRequest request = new UpdateCategoryRequest();
            request.setDescription("  Updated Description  ");
            when(categoryRepository.findByIdAndUser_Id(categoryId, USER_ID))
                    .thenReturn(Optional.of(category));

            CategoryResponse result = categoryService.update(categoryId, request, USER_ID);

            assertThat(result).isNotNull();
            assertThat(category.getDescription()).isEqualTo("Updated Description");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when category not found")
        void notFound_throws() {
            String unknownId = UUID.randomUUID().toString();
            when(categoryRepository.findByIdAndUser_Id(unknownId, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(unknownId, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category not found");
        }

        @Test
        @DisplayName("deletes when owner matches")
        void success() throws Exception {
            when(categoryRepository.findByIdAndUser_Id(categoryId, USER_ID))
                    .thenReturn(Optional.of(category));

            categoryService.delete(categoryId, USER_ID);

            verify(categoryRepository).delete(category);
        }
    }

    @Nested
    @DisplayName("findAllForAdmin")
    class FindAllForAdmin {

        @Test
        @DisplayName("returns all categories")
        void returnsList() {
            when(categoryRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(category));

            List<CategoryResponse> result = categoryService.findAllForAdmin();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(categoryId);
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }
}
