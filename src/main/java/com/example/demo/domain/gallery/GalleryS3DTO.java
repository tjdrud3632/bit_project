package com.example.demo.domain.gallery;

import lombok.Data;

@Data
public class GalleryS3DTO {

    private Long fno;
    private Long bno;
    private String fileName;
    private String url;

}
