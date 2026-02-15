package com.example.backend.service;

import com.example.backend.dto.ImageUploadRequest;
import com.example.backend.dto.ImageUploadResponse;
import com.example.backend.entity.ImageUpload;
import com.example.backend.entity.User;
import com.example.backend.repository.ImageUploadRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    /** Minimal 1x1 PNG in Base64. */
    private static final String VALID_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    @Mock
    private ImageUploadRepository imageUploadRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImageUploadService imageUploadService;

    private static final Long USER_ID = 1L;
    private User user;
    private ImageUpload imageUpload;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageUploadService, "baseUrl", "http://localhost:8081");

        user = new User();
        user.setId(USER_ID);
        user.setEmail("user@example.com");

        imageUpload = new ImageUpload();
        imageUpload.setId(10L);
        imageUpload.setShortCode("abc12def");
        imageUpload.setFilePath(USER_ID + "/abc12def");
        imageUpload.setContentType("image/png");
        imageUpload.setOriginalFileName("test.png");
        imageUpload.setUser(user);
    }

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("throws when user not found")
        void userNotFound_throws() {
            ImageUploadRequest request = new ImageUploadRequest();
            request.setBase64(VALID_PNG_BASE64);
            request.setContentType("image/png");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageUploadService.upload(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("throws when base64 is invalid or empty")
        void invalidBase64_throws() {
            ImageUploadRequest request = new ImageUploadRequest();
            request.setBase64("not-valid-base64!!!");
            request.setContentType("image/png");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> imageUploadService.upload(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid or empty Base64 image data");
        }

        @Test
        @DisplayName("saves file and entity when given valid base64")
        void success(@TempDir Path tempDir) throws IOException {
            ReflectionTestUtils.setField(imageUploadService, "imageDir", tempDir.toString());
            ImageUploadRequest request = new ImageUploadRequest();
            request.setBase64(VALID_PNG_BASE64);
            request.setContentType("image/png");
            request.setOriginalFileName("logo.png");

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(imageUploadRepository.existsByShortCode(any())).thenReturn(false);
            when(imageUploadRepository.save(any(ImageUpload.class))).thenAnswer(inv -> {
                ImageUpload e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            ImageUploadResponse response = imageUploadService.upload(request, USER_ID);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getContentType()).isEqualTo("image/png");
            assertThat(response.getOriginalFileName()).isEqualTo("logo.png");
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getImageUrl()).startsWith("http://localhost:8081/i/");
            assertThat(response.getShortCode()).hasSize(8);
            verify(imageUploadRepository).save(any(ImageUpload.class));
            assertThat(tempDir.resolve(String.valueOf(USER_ID))).exists();
        }

        @Test
        @DisplayName("extracts contentType from data URL when not provided")
        void dataUrlContentType(@TempDir Path tempDir) throws IOException {
            ReflectionTestUtils.setField(imageUploadService, "imageDir", tempDir.toString());
            ImageUploadRequest request = new ImageUploadRequest();
            request.setBase64("data:image/jpeg;base64," + VALID_PNG_BASE64);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(imageUploadRepository.existsByShortCode(any())).thenReturn(false);
            when(imageUploadRepository.save(any(ImageUpload.class))).thenAnswer(inv -> {
                ImageUpload e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            ImageUploadResponse response = imageUploadService.upload(request, USER_ID);

            assertThat(response.getContentType()).isEqualTo("image/jpeg");
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("returns list of ImageUploadResponse for user")
        void returnsList() {
            when(imageUploadRepository.findByUser_IdOrderByCreatedAtDesc(USER_ID))
                    .thenReturn(List.of(imageUpload));

            List<ImageUploadResponse> result = imageUploadService.findAllByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getShortCode()).isEqualTo("abc12def");
            assertThat(result.get(0).getImageUrl()).isEqualTo("http://localhost:8081/i/abc12def");
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("throws when image upload not found")
        void notFound_throws() {
            when(imageUploadRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageUploadService.findById(99L, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Image upload not found");
        }

        @Test
        @DisplayName("throws when image belongs to another user")
        void wrongUser_throws() {
            when(imageUploadRepository.findById(10L)).thenReturn(Optional.of(imageUpload));

            assertThatThrownBy(() -> imageUploadService.findById(10L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Image upload not found");
        }

        @Test
        @DisplayName("returns response when owner matches")
        void success() {
            when(imageUploadRepository.findById(10L)).thenReturn(Optional.of(imageUpload));

            ImageUploadResponse result = imageUploadService.findById(10L, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getShortCode()).isEqualTo("abc12def");
        }
    }

    @Nested
    @DisplayName("findAllForAdmin")
    class FindAllForAdmin {

        @Test
        @DisplayName("returns all image uploads")
        void returnsList() {
            when(imageUploadRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(imageUpload));

            List<ImageUploadResponse> result = imageUploadService.findAllForAdmin();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("resolveByShortCode")
    class ResolveByShortCode {

        @Test
        @DisplayName("throws when short code not found")
        void notFound_throws() {
            when(imageUploadRepository.findByShortCode("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageUploadService.resolveByShortCode("missing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Image not found");
        }

        @Test
        @DisplayName("returns entity and normalizes code to lowercase")
        void success() {
            when(imageUploadRepository.findByShortCode("abc12def")).thenReturn(Optional.of(imageUpload));

            ImageUpload result = imageUploadService.resolveByShortCode("ABC12DEF");

            assertThat(result).isNotNull();
            assertThat(result.getShortCode()).isEqualTo("abc12def");
            verify(imageUploadRepository).findByShortCode("abc12def");
        }
    }

    @Nested
    @DisplayName("getAbsoluteFilePath")
    class GetAbsoluteFilePath {

        @Test
        @DisplayName("returns path under image dir")
        void returnsPath() {
            ReflectionTestUtils.setField(imageUploadService, "imageDir", "/var/uploads/images");

            Path path = imageUploadService.getAbsoluteFilePath(imageUpload);

            assertThat(path).isEqualTo(Path.of("/var/uploads/images").resolve("1/abc12def"));
        }
    }
}
