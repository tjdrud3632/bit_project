package com.example.demo.service.gallery;

import com.example.demo.domain.Criteria;
import com.example.demo.domain.gallery.GalleryFileVO;
import com.example.demo.domain.gallery.GalleryVO;

import java.util.List;

public interface GalleryService {

    public void register(GalleryVO gallery);

    public GalleryVO get(Long bno);

    public boolean modify(GalleryVO gallery);

    public boolean remove(Long bno);

   // public List<GalleryVO> getList();

    public List<GalleryVO> getList(Criteria cri);

    public List<GalleryFileVO> getFileList(Long bno);

    public int getTotal (Criteria cri);
}
