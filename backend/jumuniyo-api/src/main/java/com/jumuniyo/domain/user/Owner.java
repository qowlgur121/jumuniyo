package com.jumuniyo.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "owners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // 사용자 정보 참조

    @Column(nullable = false, length = 20, unique = true)
    private String businessNumber; // 사업자등록번호

    @Column(nullable = false, length = 100)
    private String storeName; // 가게명

    @Column(nullable = false, length = 255)
    private String storeAddress; // 가게 주소

    @Column(length = 100)
    private String storeAddressDetail; // 상세 주소

    @Column(nullable = false, length = 20)
    private String storePhoneNumber; // 가게 전화번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; // 사장님 승인 상태

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Owner(User user, String businessNumber, String storeName, String storeAddress, 
                String storeAddressDetail, String storePhoneNumber, UserStatus status) {
        this.user = user;
        this.businessNumber = businessNumber;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeAddressDetail = storeAddressDetail;
        this.storePhoneNumber = storePhoneNumber;
        this.status = status;
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    public void updateStoreInfo(String storeName, String storeAddress, String storeAddressDetail, String storePhoneNumber) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storeAddressDetail = storeAddressDetail;
        this.storePhoneNumber = storePhoneNumber;
    }
} 