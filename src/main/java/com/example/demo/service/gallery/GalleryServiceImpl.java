package com.example.demo.service.gallery;


import com.example.demo.domain.Criteria;
import com.example.demo.domain.gallery.GalleryFileVO;
import com.example.demo.domain.gallery.GalleryVO;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.entity.MemberRepository;
import com.example.demo.mapper.gallery.GalleryFileMapper;
import com.example.demo.mapper.gallery.GalleryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GalleryServiceImpl implements GalleryService {

    @Autowired
    private GalleryMapper mapper;

    @Autowired
    private GalleryFileMapper fileMapper;

    @Autowired
    private MemberRepository repository;



    @Transactional
    @Override
    public void register(GalleryVO gallery) {

        Member member = repository.findByMemberId(gallery.getWriter());
        Long memberNO = member.getMemberNo();
        gallery.setMno(memberNO);

        mapper.insertSelectKey(gallery);

//file 없다
        if(gallery.getFileList() == null || gallery.getFileList().size() <= 0){
            return;
        }
//게시글번호를 fileVO객체 bno에 삽입, 파일 insert
        gallery.getFileList().forEach(file -> {

            file.setBno(gallery.getBno());
            fileMapper.insert(file);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public GalleryVO get(Long bno) {
        mapper.updateHit(bno);
        return mapper.read(bno);
    }

    @Transactional
    @Override
    public boolean modify(GalleryVO gallery) {
//다 지워주고
        fileMapper.deleteAll(gallery.getBno());
//update가 성공이면
        boolean modifyResult = mapper.update(gallery) == 1;
//update가 성공적이고 파일이 비어있지 않다면 (파일이 있다면)
        if(modifyResult && gallery.getFileList() != null && gallery.getFileList().size() > 0){
            gallery.getFileList().forEach(file ->{
//다시 게시글 번호와 파일을 삽입함
                file.setBno(gallery.getBno());
                fileMapper.insert(file);
            });
        }
        return modifyResult;
    }
    @Transactional
    @Override
    public boolean remove(Long bno) {
        fileMapper.deleteAll(bno);
        return mapper.delete(bno) == 1;
    }

    @Override
    public List<GalleryVO> getList(Criteria cri) {

        return mapper.getListWithPaging(cri);
    }

    @Override
    public List<GalleryFileVO> getFileList(Long bno) {
        return fileMapper.findByBno(bno);
    }

    @Override
    public int getTotal(Criteria cri) {
        return mapper.getTotalCount(cri);
    }



  /*  @Override
    public List<GalleryFileVO> getFileList(Long bno) {
        return null;
    }*/




}
