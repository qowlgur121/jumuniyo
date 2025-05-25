package com.jumuniyo.repository.user;

import com.jumuniyo.domain.user.Owner;
import com.jumuniyo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    
    boolean existsByBusinessNumber(String businessNumber);
    
    Optional<Owner> findByUser(User user);
    
    Optional<Owner> findByUserId(Long userId);
} 