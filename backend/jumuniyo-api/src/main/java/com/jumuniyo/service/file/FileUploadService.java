package com.jumuniyo.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    /**
     * 메뉴 이미지 업로드
     */
    String uploadMenuImage(MultipartFile file, Long storeId, Long menuId);

    /**
     * 가게 로고 이미지 업로드
     */
    String uploadStoreLogoImage(MultipartFile file, Long storeId);

    /**
     * 파일 삭제
     */
    void deleteFile(String fileUrl);

    /**
     * 파일 유효성 검사
     */
    boolean isValidImageFile(MultipartFile file);
} 