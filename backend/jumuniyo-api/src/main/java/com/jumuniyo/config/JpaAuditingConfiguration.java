package com.jumuniyo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EntityListeners와 필드 어노테이션만으로는 기능이 작동하지 않고,
 * @EnableJpaAuditing이 @Configuration 클래스에 붙어서 스프링에게 '기능 활성화' 지시를 내려야
 * 비로소 모든 것이 연결되어 작동하게 된다.
 */
@Configuration
@EnableJpaAuditing // JPA Auditing 기능 활성화
public class JpaAuditingConfiguration {
    // 이 클래스는 비어 있어도 된다. 어노테이션 자체가 설정 역할을 한다.
}