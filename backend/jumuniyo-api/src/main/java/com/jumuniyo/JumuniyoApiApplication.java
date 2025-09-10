package com.jumuniyo;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import com.jumuniyo.repository.store.CategoryRepository;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@SpringBootApplication
@RequiredArgsConstructor
public class JumuniyoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JumuniyoApiApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(CategoryRepository categoryRepository, UserRepository userRepository, 
							   StoreRepository storeRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// 카테고리가 없을 때만 초기 데이터 추가
			if (categoryRepository.count() == 0) {
				categoryRepository.save(Category.builder()
						.name("한식")
						.description("한국 전통 음식")
						.displayOrder(1)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("중식")
						.description("중국 음식")
						.displayOrder(2)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("일식")
						.description("일본 음식")
						.displayOrder(3)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("양식")
						.description("서양 음식")
						.displayOrder(4)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("치킨")
						.description("치킨 전문점")
						.displayOrder(5)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("피자")
						.description("피자 전문점")
						.displayOrder(6)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("족발/보쌈")
						.description("족발 보쌈 전문점")
						.displayOrder(7)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("야식")
						.description("야식 전문점")
						.displayOrder(8)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("카페/디저트")
						.description("카페 및 디저트")
						.displayOrder(9)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("패스트푸드")
						.description("패스트푸드")
						.displayOrder(10)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("버거")
						.description("햄버거 전문점")
						.displayOrder(11)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("일식/돈까스")
						.description("일식 및 돈까스")
						.displayOrder(12)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("고기/구이")
						.description("고기 및 구이 요리")
						.displayOrder(13)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("샐러드")
						.description("샐러드 전문점")
						.displayOrder(14)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("도시락/죽")
						.description("도시락 및 죽 전문점")
						.displayOrder(15)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("아시안")
						.description("아시아 음식")
						.displayOrder(16)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("1인분주문")
						.description("1인분 주문 가능")
						.displayOrder(17)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("프랜차이즈")
						.description("프랜차이즈 브랜드")
						.displayOrder(18)
						.build());
				
				categoryRepository.save(Category.builder()
						.name("신규맛집")
						.description("새로 오픈한 맛집")
						.displayOrder(19)
						.build());
				
				System.out.println("카테고리 초기 데이터가 추가되었습니다.");
			}
			
			// 사용자가 없을 때만 초기 사용자 추가
			if (userRepository.count() == 0) {
				User owner = User.builder()
						.email("owner@test.com")
						.password(passwordEncoder.encode("password123"))
						.nickname("테스트사장님")
						.phoneNumber("010-1234-5678")
						.role(UserRole.ROLE_OWNER)
						.status(UserStatus.ACTIVE)
						.build();
				userRepository.save(owner);
				System.out.println("테스트 사용자가 추가되었습니다.");
				
				// 가게가 없을 때만 초기 가게 추가
				if (storeRepository.count() == 0) {
					Category category = categoryRepository.findByName("한식").orElse(null);
					if (category != null) {
						Store store = Store.builder()
								.name("테스트 한식당")
								.description("맛있는 한식을 제공하는 테스트 음식점입니다.")
								.address("서울시 강남구 테스트로 123")
								.phoneNumber("02-1234-5678")
								.businessNumber("123-45-67890")
								.minimumOrderAmount(new BigDecimal("15000"))
								.deliveryFee(new BigDecimal("3000"))
								.category(category)
								.owner(owner)
								.build();
						storeRepository.save(store);
						System.out.println("테스트 가게가 추가되었습니다.");
					}
				}
			}
		};
	}
}
