package com.picturex.repository;

import com.picturex.entity.Image;
import com.picturex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUser(User user);
    
    @Query("SELECT i FROM Image i WHERE i.user = :user AND " +
           "(:keyword IS NULL OR i.originalFilename LIKE %:keyword% OR i.description LIKE %:keyword%) AND " +
           "(:startDate IS NULL OR i.uploadDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.uploadDate <= :endDate)")
    List<Image> searchImages(@Param("user") User user,
                             @Param("keyword") String keyword,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM Image i JOIN i.tags t WHERE i.user = :user AND t.name = :tagName")
    List<Image> findByUserAndTag(@Param("user") User user, @Param("tagName") String tagName);
}

