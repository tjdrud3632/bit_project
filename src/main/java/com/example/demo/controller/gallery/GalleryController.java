package com.example.demo.controller.gallery;

import com.example.demo.config.SessionMember;
import com.example.demo.domain.Criteria;
import com.example.demo.domain.PageDTO;
import com.example.demo.domain.gallery.GalleryFileVO;
import com.example.demo.domain.gallery.GalleryVO;
import com.example.demo.service.gallery.GalleryService;
import com.example.demo.service.gallery.ReplyService1;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/gallery/")
@AllArgsConstructor
public class GalleryController {

    @Autowired
    private GalleryService service;

    @Autowired
    private ReplyService1 replyService1;

    @GetMapping("register")
    public String register(Model model, HttpSession httpSession){

        SessionMember socialMember = (SessionMember) httpSession.getAttribute("socialMember");
        SessionMember loginMember = (SessionMember) httpSession.getAttribute("loginMember");

        if(socialMember != null){
            model.addAttribute("member", socialMember);
        } else if (loginMember != null){
            model.addAttribute("member", loginMember);
        }
        return "gallery/register";
    }

    @PostMapping("register")
    public String register(GalleryVO gallery, RedirectAttributes rttr){

        service.register(gallery);
        rttr.addFlashAttribute("result", gallery.getBno());

        return "redirect:list";
    }

    @GetMapping("list") //회원인지 체크 회원이 아니면 로그인페이지로 리턴
    public String list(Criteria cri, Model model, HttpSession httpSession){

        SessionMember socialMember = (SessionMember) httpSession.getAttribute("socialMember");
        SessionMember loginMember =(SessionMember) httpSession.getAttribute("loginMember");

        if (loginMember == null && socialMember == null){
            return "redirect:/login";
        }

         else {
            List<GalleryVO> list = service.getList(cri);
            for (GalleryVO galleryVO : list) {
//                galleryVO.setFileList(service.getFileList(galleryVO.getBno()));
                if(service.getFileList(galleryVO.getBno()).size() > 0) {
                    System.out.println("list::" + list);
                    GalleryFileVO fileVO = service.getFileList(galleryVO.getBno()).get(0);
                    galleryVO.setFileUrl(fileVO.getUploadPath().replace("\\", "/") + "/" + fileVO.getUuid() + "_" + fileVO.getFileName());
                }
        }

            model.addAttribute("list", list);
            int total = service.getTotal(cri);
            model.addAttribute("pageMaker", new PageDTO(cri, total));
            return "gallery/list";
        }
    }


    @GetMapping("get")
    public String get(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri, Model model, HttpSession httpSession){

        model.addAttribute("gallery", service.get(bno));
        return "gallery/get";
    }




    @GetMapping("modify")
    public String modify(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri, Model model){
        model.addAttribute("gallery", service.get(bno));

        return "gallery/modify";
    }

    @PostMapping("modify")
    public String modify(GalleryVO gallery, @ModelAttribute("cri") Criteria cri, RedirectAttributes rttr){

        if(service.modify(gallery)){
            rttr.addFlashAttribute("result", "success");
        }

        return "redirect:list" + cri.getListLink();
    }

    @PostMapping("remove")
    public String remove(@RequestParam("bno") Long bno,Criteria cri, RedirectAttributes rttr ){

        List<GalleryFileVO> fileList = service.getFileList(bno);

        if(service.remove(bno)){
            deleteFiles(fileList);

            rttr.addAttribute("result","success");
        }

        return "redirect:list" + cri.getListLink();
    }

    private void deleteFiles(List<GalleryFileVO> fileList){
//없으면 그냥 리턴
        if(fileList == null || fileList.size() == 0){
            return;
        }

        fileList.forEach(file -> {
            try {
       //파일 삭제
            Path filePath = Paths.get("C:\\upload\\"+ file.getUploadPath()+"\\"+file.getUuid()+"_"+ file.getFileName());

             Files.deleteIfExists(filePath); //boolean
        //이미지면 썸네일 파일도 삭제해줌
            if(Files.probeContentType(filePath).startsWith("image"));
                Path thumbNail = Paths.get("C:\\upload\\"+ file.getUploadPath()+"\\s_"+file.getUuid()+"_"+ file.getFileName());

                Files.delete(thumbNail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //첨부파일과 관련된 내용을 json형태로 가지고옴 // $.getJSON("/gallery/getFileList", - get.jsp
    @GetMapping(value = "getFileList",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<GalleryFileVO>> getFileList(Long bno){

        return new ResponseEntity<>(service.getFileList(bno), HttpStatus.OK);
    }




}
