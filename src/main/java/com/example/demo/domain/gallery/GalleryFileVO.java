package com.example.demo.domain.gallery;

import lombok.Data;

//테이블 데이터
@Data
public class GalleryFileVO {

    private Long fno;
    private String uuid;
    private String uploadPath;
    private String fileName;
    private boolean fileType;

    private Long bno;

}
