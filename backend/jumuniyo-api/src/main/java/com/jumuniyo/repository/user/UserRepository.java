package com.jumuniyo.repository.user; // UserRepository.java 파일이 위치한 폴더(패키지)를 나타내는 것임. 본인의 프로젝트 구조에 맞게 수정함.

import com.jumuniyo.domain.user.User; // 우리가 만든 User Entity 설계도를 가져옴.
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA가 제공하는 기본 데이터 관리 기능 목록(인터페이스)을 가져옴.
// import org.springframework.stereotype.Repository; // 이 어노테이션은 Spring Data JPA에서는 보통 생략해도 됨.

import java.util.Optional; // 데이터 조회 결과가 '있을 수도 있고 없을 수도 있을 때' 안전하게 처리하기 위한 자바 기능을 가져옴.

// public interface UserRepository extends JpaRepository<User, Long> 임.
// 이 UserRepository는 JpaRepository 라는 미리 만들어진 '데이터 관리 기본 기능 목록'을 **상속받아서** 사용할 것임.
// <User, Long> 이라고 되어 있는 것은, 이 JpaRepository가 'User Entity'를 관리할 거고, User의 주민등록번호 격인 '기본 키(ID)' 타입은 'Long' 이야 라고 알려주는 것임.
// @Repository // Spring Data JPA는 JpaRepository를 상속받은 인터페이스는 자동으로 Spring이 관리하는 '빈(Bean)'으로 등록해줌. 그래서 굳이 이 어노테이션을 붙이지 않아도 잘 작동함.
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<T, ID> 를 상속받는 것만으로도 이미 아래 기능들이 자동으로 생겨난 것임:
    // - save(User user): 사용자 정보를 데이터베이스에 저장 또는 수정하는 기능
    // - findById(Long id): id(고유 번호)로 사용자를 찾아오는 기능 (결과가 없을 수도 있으니 Optional 로 감싸서 줌)
    // - findAll(): 데이터베이스에 저장된 모든 사용자를 찾아오는 기능
    // - delete(User user): 사용자 정보를 삭제하는 기능
    // - count(): 저장된 사용자 수를 세는 기능
    // 등등... 기본적인 데이터 관리 기능들이 이미 준비된 것임!

    // --- Spring Data JPA 쿼리 메소드 ---
    // Spring Data JPA는 우리가 인터페이스에 **메소드 이름만 잘 적어주면**, 그 이름에 맞춰서 데이터베이스에서 데이터를 찾아오는(쿼리) 코드를 자동으로 만들어주는 '마법' 같은 기능이 있음.

    // 이메일(email)로 사용자를 찾아오는 기능 목록에 추가하는 것임.
    // Spring Data JPA는 findByEmail 이라는 메소드 이름을 보고 '아, User Entity에서 email 필드를 가지고 데이터를 찾아오라는 거구나!' 하고 알아서 데이터베이스 쿼리를 만들어줌.
    // 데이터베이스 쿼리는 대략 이런 모양일 것임: SELECT * FROM users WHERE email = [메소드에 넘겨받은 email 값]
    // Optional<User> 임: 찾아온 사용자가 '있을 수도 있고 없을 수도 있으니', 결과가 없을 때 에러가 나지 않도록 안전하게 Optional 이라는 '상자'에 담아서 반환하는 것임.
    Optional<User> findByEmail(String email);

    // 닉네임(nickname)으로 사용자를 찾아오는 기능 목록에 추가하는 것임.
    // Spring Data JPA는 findByNickname 이라는 메소드 이름을 보고 '아, User Entity에서 nickname 필드를 가지고 데이터를 찾아오라는 거구나!' 하고 알아서 쿼리를 만들어줌.
    // 데이터베이스 쿼리는 대략 이런 모양일 것임: SELECT * FROM users WHERE nickname = [메소드에 넘겨받은 nickname 값]
    // Optional<User> 임: 찾아온 사용자가 없을 수도 있으니 Optional 로 감싸서 반환함.
    Optional<User> findByNickname(String nickname);

    // 이메일(email)을 가진 사용자가 데이터베이스에 '존재하는지' 확인하는 기능 목록에 추가하는 것임.
    // Spring Data JPA는 existsByEmail 이라는 메소드 이름을 보고 '아, User Entity에서 email 필드를 가지고 해당 데이터가 있는지 없는지만 빠르게 확인하라는 거구나!' 하고 알아서 쿼리를 만들어줌.
    // 결과는 true(있음) 또는 false(없음)로 알려줄 것임.
    boolean existsByEmail(String email);

    // 닉네임(nickname)을 가진 사용자가 데이터베이스에 '존재하는지' 확인하는 기능 목록에 추가하는 것임.
    // Spring Data JPA는 existsByNickname 이라는 메소드 이름을 보고 '아, User Entity에서 nickname 필드를 가지고 해당 데이터가 있는지 없는지만 빠르게 확인하라는 거구나!' 하고 알아서 쿼리를 만들어줌.
    // 결과는 true(있음) 또는 false(없음)로 알려줄 것임.
    boolean existsByNickname(String nickname);

    // --- 필요하다면 @Query 어노테이션을 사용하여 더 복잡한 데이터베이스 조회 코드(쿼리)를 직접 작성할 수도 있음 ---
    // 아래는 예시 코드임. 지금은 몰라도 됨.
    // @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC") // 직접 데이터 조회 코드(JPQL 이라는 자바 객체용 쿼리 언어임)를 작성하는 것임.
    // List<User> findUsersByStatusOrderByCreatedAtDesc(@Param("status") UserStatus status); // 이 메소드 이름은 쿼리 메소드 규칙을 따르지 않아도 됨. @Query 어노테이션에 적힌 쿼리가 실행됨. @Param은 쿼리 안의 :status 부분과 메소드의 status 파라미터를 연결해주는 것임.
}