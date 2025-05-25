package com.jumuniyo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumuniyo.JumuniyoApiApplication;
import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.repository.store.CategoryRepository;
import com.jumuniyo.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = JumuniyoApiApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("음식점 API 통합 테스트")
public class StoreIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testOwner;
    private User testAdmin;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트용 카테고리 생성
        testCategory = Category.builder()
                .name("한식")
                .description("한국 음식")
                .displayOrder(1)
                .build();
        categoryRepository.save(testCategory);

        // 테스트용 사장님 사용자 생성
        testOwner = User.builder()
                .email("owner@test.com")
                .password(passwordEncoder.encode("password"))
                .nickname("테스트 사장님")
                .phoneNumber("010-1234-5678")
                .role(UserRole.ROLE_OWNER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(testOwner);

        // 테스트용 관리자 생성
        testAdmin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("password"))
                .nickname("테스트 관리자")
                .phoneNumber("010-9999-9999")
                .role(UserRole.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(testAdmin);
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("음식점 등록 성공 테스트")
    void createStore_Success() throws Exception {
        // Given
        StoreCreateRequestDto requestDto = StoreCreateRequestDto.builder()
                .name("맛있는 음식점")
                .address("서울시 강남구 테헤란로 123")
                .description("정말 맛있는 음식점입니다")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .categoryId(testCategory.getId())
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("맛있는 음식점")))
                .andExpect(jsonPath("$.businessNumber", is("123-45-67890")))
                .andExpect(jsonPath("$.isApproved", is(false)))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.minimumOrderAmount", is(15000)))
                .andExpect(jsonPath("$.deliveryFee", is(3000)));
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("음식점 등록 실패 - 유효성 검증 오류")
    void createStore_ValidationError() throws Exception {
        // Given - 필수 필드 누락
        StoreCreateRequestDto requestDto = StoreCreateRequestDto.builder()
                .name("") // 빈 이름
                .businessNumber("123") // 잘못된 사업자등록번호 형식
                .phoneNumber("invalid") // 잘못된 전화번호 형식
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("음식점 조회 성공 테스트")
    void getStore_Success() throws Exception {
        // Given
        Store store = Store.builder()
                .name("테스트 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // When & Then
        mockMvc.perform(get("/api/v1/stores/{storeId}", savedStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedStore.getId().intValue())))
                .andExpect(jsonPath("$.name", is("테스트 음식점")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("내 음식점 목록 조회 성공 테스트")
    void getMyStores_Success() throws Exception {
        // Given
        Store store1 = Store.builder()
                .name("음식점1")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();

        Store store2 = Store.builder()
                .name("음식점2")
                .address("서울시 서초구")
                .businessNumber("987-65-43210")
                .phoneNumber("02-9876-5432")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(20000))
                .deliveryFee(BigDecimal.valueOf(4000))
                .build();

        storeRepository.save(store1);
        storeRepository.save(store2);

        // When & Then - 페이징 응답 구조 고려
        mockMvc.perform(get("/api/v1/stores/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("음식점1")))
                .andExpect(jsonPath("$.content[1].name", is("음식점2")));
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("음식점 수정 성공 테스트")
    void updateStore_Success() throws Exception {
        // Given
        Store store = Store.builder()
                .name("원래 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // StoreUpdateRequestDto는 모든 필드가 필수이므로 전체 데이터 제공
        StoreUpdateRequestDto updateDto = StoreUpdateRequestDto.builder()
                .name("수정된 음식점")
                .description("새로운 설명")
                .address("서울시 강남구")  // 필수 필드
                .businessNumber("123-45-67890")  // 필수 필드
                .phoneNumber("02-9999-8888")
                .minimumOrderAmount(BigDecimal.valueOf(20000))
                .deliveryFee(BigDecimal.valueOf(4000))
                .categoryId(testCategory.getId())  // 필수 필드
                .build();

        // When & Then
        mockMvc.perform(put("/api/v1/stores/{storeId}", savedStore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("수정된 음식점")))
                .andExpect(jsonPath("$.description", is("새로운 설명")))
                .andExpect(jsonPath("$.phoneNumber", is("02-9999-8888")))
                .andExpect(jsonPath("$.minimumOrderAmount", is(20000)))
                .andExpect(jsonPath("$.deliveryFee", is(4000)));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("음식점 승인 성공 테스트")
    void approveStore_Success() throws Exception {
        // Given
        Store store = Store.builder()
                .name("승인 대기 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // When & Then - 응답이 Map<String, String> 형태
        mockMvc.perform(post("/api/v1/stores/{storeId}/approve", savedStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("음식점이 승인되었습니다.")));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("음식점 승인 거부 성공 테스트")
    void rejectStore_Success() throws Exception {
        // Given
        Store store = Store.builder()
                .name("승인 대기 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // When & Then - 응답이 Map<String, String> 형태
        mockMvc.perform(post("/api/v1/stores/{storeId}/reject", savedStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("음식점 승인이 거부되었습니다.")));
    }

    @Test
    @WithMockUser(username = "owner@test.com", roles = "OWNER")
    @DisplayName("음식점 삭제(비활성화) 성공 테스트")
    void deleteStore_Success() throws Exception {
        // Given
        Store store = Store.builder()
                .name("삭제할 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // When & Then - 응답이 Map<String, String> 형태
        mockMvc.perform(delete("/api/v1/stores/{storeId}", savedStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("음식점이 성공적으로 삭제되었습니다.")));
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "OWNER")
    @DisplayName("존재하지 않는 사용자의 음식점 수정 시도 - 사용자 없음 오류")
    void updateStore_UserNotFound() throws Exception {
        // Given
        Store store = Store.builder()
                .name("다른 사용자 음식점")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .category(testCategory)
                .owner(testOwner)
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .build();
        Store savedStore = storeRepository.save(store);

        // 유효한 데이터로 테스트 - 하지만 사용자가 존재하지 않음
        StoreUpdateRequestDto updateDto = StoreUpdateRequestDto.builder()
                .name("해킹 시도")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .categoryId(testCategory.getId())
                .build();

        // When & Then - 사용자를 찾을 수 없어서 400 응답
        mockMvc.perform(put("/api/v1/stores/{storeId}", savedStore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest()); // "사용자를 찾을 수 없습니다" 오류
    }

    @Test
    @DisplayName("인증 없이 음식점 등록 시도 - 인증 오류")
    void createStore_Unauthenticated() throws Exception {
        // Given
        StoreCreateRequestDto requestDto = StoreCreateRequestDto.builder()
                .name("무단 등록 시도")
                .address("서울시 강남구")
                .businessNumber("123-45-67890")
                .phoneNumber("02-1234-5678")
                .minimumOrderAmount(BigDecimal.valueOf(15000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .categoryId(testCategory.getId())
                .build();

        // When & Then - Spring Security 리다이렉트로 인해 302 응답
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isFound()); // 302 Found (리다이렉트)
    }
} 