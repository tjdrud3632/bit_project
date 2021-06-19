package com.example.demo.mapper.gallery;

import com.example.demo.domain.Criteria;
import com.example.demo.domain.gallery.ReplyVO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyMapper1 {

    public int insert(ReplyVO reply);

    public ReplyVO read(Long bno);

    public int delete(Long bno);

    public int update(ReplyVO reply);

    public List<ReplyVO> getListWithPaging(@Param("cri")Criteria cri, @Param("bno") Long bno);

    public int getCountByBno(Long bno);
}
