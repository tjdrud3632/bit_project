package com.example.demo.mapper.gallery;

import com.example.demo.domain.gallery.GalleryFileVO;

import java.util.List;

public interface GalleryFileMapper {

    public void insert(GalleryFileVO file);

    public void delete(Long fno);

    public List<GalleryFileVO> findByBno(Long bno);

    public void deleteAll(Long bno);

    public List<GalleryFileVO> getOldFiles();
}
