package com.example.backend.service;

import com.example.backend.dto.CreateShortenerRequest;
import com.example.backend.dto.ShortenerResponse;
import com.example.backend.dto.UpdateShortenerRequest;
import com.example.backend.entity.Shortener;
import com.example.backend.entity.User;
import com.example.backend.repository.ShortenerRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortenerServiceTest {

    @Mock
    private ShortenerRepository shortenerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShortenerService shortenerService;

    private static final Long USER_ID = 1L;
    private User user;
    private Shortener shortener;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("user@example.com");

        shortener = new Shortener();
        shortener.setId(10L);
        shortener.setShortCode("abc12345");
        shortener.setFullUrl("https://example.com/page");
        shortener.setUser(user);
        shortener.setClickCount(0);
        shortener.setActive(true);
        shortener.setExpiresAt(null);
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("returns list of ShortenerResponse for user")
        void returnsList() {
            when(shortenerRepository.findByUser_IdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(shortener));

            List<ShortenerResponse> result = shortenerService.findAllByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(10L);
            assertThat(result.get(0).getShortCode()).isEqualTo("abc12345");
            assertThat(result.get(0).getFullUrl()).isEqualTo("https://example.com/page");
            assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("throws when shortener not found")
        void notFound_throws() {
            when(shortenerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortenerService.findById(99L, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link not found");
        }

        @Test
        @DisplayName("throws when shortener belongs to another user")
        void wrongUser_throws() {
            when(shortenerRepository.findById(10L)).thenReturn(Optional.of(shortener));

            assertThatThrownBy(() -> shortenerService.findById(10L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link not found");
        }

        @Test
        @DisplayName("returns ShortenerResponse when owner matches")
        void success() {
            when(shortenerRepository.findById(10L)).thenReturn(Optional.of(shortener));

            ShortenerResponse result = shortenerService.findById(10L, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getShortCode()).isEqualTo("abc12345");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("throws when user not found")
        void userNotFound_throws() {
            CreateShortenerRequest request = new CreateShortenerRequest();
            request.setFullUrl("https://example.com");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortenerService.create(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("throws when short code already in use")
        void shortCodeTaken_throws() {
            CreateShortenerRequest request = new CreateShortenerRequest();
            request.setFullUrl("https://example.com");
            request.setShortCode("taken123");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(shortenerRepository.existsByShortCode("taken123")).thenReturn(true);

            assertThatThrownBy(() -> shortenerService.create(request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short code already in use: taken123");
        }

        @Test
        @DisplayName("prepends https when fullUrl has no scheme")
        void addsHttpsWhenNoScheme() {
            CreateShortenerRequest request = new CreateShortenerRequest();
            request.setFullUrl("example.com/path");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(shortenerRepository.existsByShortCode(any())).thenReturn(false);
            when(shortenerRepository.save(any(Shortener.class))).thenAnswer(inv -> {
                Shortener s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            ShortenerResponse result = shortenerService.create(request, USER_ID);

            assertThat(result).isNotNull();
            ArgumentCaptor<Shortener> captor = ArgumentCaptor.forClass(Shortener.class);
            verify(shortenerRepository).save(captor.capture());
            assertThat(captor.getValue().getFullUrl()).isEqualTo("https://example.com/path");
        }

        @Test
        @DisplayName("saves with custom short code when provided")
        void withShortCode_success() {
            CreateShortenerRequest request = new CreateShortenerRequest();
            request.setFullUrl("https://example.com");
            request.setShortCode("  mycode  ");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(shortenerRepository.existsByShortCode("mycode")).thenReturn(false);
            when(shortenerRepository.save(any(Shortener.class))).thenAnswer(inv -> {
                Shortener s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            ShortenerResponse result = shortenerService.create(request, USER_ID);

            assertThat(result).isNotNull();
            ArgumentCaptor<Shortener> captor = ArgumentCaptor.forClass(Shortener.class);
            verify(shortenerRepository).save(captor.capture());
            assertThat(captor.getValue().getShortCode()).isEqualTo("mycode");
            assertThat(captor.getValue().getFullUrl()).isEqualTo("https://example.com");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("throws when short link not found or wrong user")
        void notFound_throws() {
            UpdateShortenerRequest request = new UpdateShortenerRequest();
            when(shortenerRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortenerService.update(10L, request, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link not found");
        }

        @Test
        @DisplayName("updates fullUrl and saves")
        void success() {
            UpdateShortenerRequest request = new UpdateShortenerRequest();
            request.setFullUrl("https://newurl.com");
            request.setActive(false);
            when(shortenerRepository.findById(10L)).thenReturn(Optional.of(shortener));
            when(shortenerRepository.save(any(Shortener.class))).thenReturn(shortener);

            ShortenerResponse result = shortenerService.update(10L, request, USER_ID);

            assertThat(result).isNotNull();
            assertThat(shortener.getFullUrl()).isEqualTo("https://newurl.com");
            assertThat(shortener.isActive()).isFalse();
            verify(shortenerRepository).save(shortener);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("throws when short link not found")
        void notFound_throws() {
            when(shortenerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortenerService.delete(99L, USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link not found");
        }

        @Test
        @DisplayName("deletes when owner matches")
        void success() {
            when(shortenerRepository.findById(10L)).thenReturn(Optional.of(shortener));

            shortenerService.delete(10L, USER_ID);

            verify(shortenerRepository).delete(shortener);
        }
    }

    @Nested
    @DisplayName("resolveAndIncrementClick")
    class ResolveAndIncrementClick {

        @Test
        @DisplayName("throws when short code not found")
        void notFound_throws() {
            when(shortenerRepository.findByShortCode("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shortenerService.resolveAndIncrementClick("missing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link not found");
        }

        @Test
        @DisplayName("throws when short link disabled")
        void disabled_throws() {
            shortener.setActive(false);
            when(shortenerRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortener));

            assertThatThrownBy(() -> shortenerService.resolveAndIncrementClick("abc12345"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link is disabled");
        }

        @Test
        @DisplayName("throws when expired")
        void expired_throws() {
            shortener.setExpiresAt(Instant.now().minusSeconds(1));
            when(shortenerRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortener));

            assertThatThrownBy(() -> shortenerService.resolveAndIncrementClick("abc12345"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Short link has expired");
        }

        @Test
        @DisplayName("returns fullUrl and increments click count")
        void success() {
            when(shortenerRepository.findByShortCode("abc12345")).thenReturn(Optional.of(shortener));
            when(shortenerRepository.save(any(Shortener.class))).thenReturn(shortener);

            String url = shortenerService.resolveAndIncrementClick("abc12345");

            assertThat(url).isEqualTo("https://example.com/page");
            assertThat(shortener.getClickCount()).isEqualTo(1);
            verify(shortenerRepository).save(shortener);
        }
    }
}
