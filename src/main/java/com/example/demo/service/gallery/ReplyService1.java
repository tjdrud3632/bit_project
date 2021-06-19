package com.example.demo.service.gallery;

import com.example.demo.domain.Criteria;
import com.example.demo.domain.gallery.ReplyPageDTO;
import com.example.demo.domain.gallery.ReplyVO;

import java.util.List;

public interface ReplyService1 {

    public int register(ReplyVO reply);

    public ReplyVO get(Long rno);

    public int modify(ReplyVO reply);

    public int remove(Long rno);

    public List<ReplyVO> getList(Criteria cri, Long bno);

    public ReplyPageDTO getListPage(Criteria cri, Long bno);


}
