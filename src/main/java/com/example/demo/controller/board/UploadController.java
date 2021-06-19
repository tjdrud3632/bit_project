package com.example.demo.controller.board;

import com.example.demo.domain.board.AttachFileDTO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.commons.CommonsMultipartResolver;
//import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class UploadController {

    @GetMapping("/uploadForm")
    public String uploadForm() {

        return "uploadForm";
    }   //uploadForm.jsp 띄어줌

    //form에서 넘어오는 uploadFormAction (uploadForm.jsp)
    //입력한 파일을 디렉토리에 저장해줌
    @PostMapping("/uploadFormAction")
    public void uploadFormPost(MultipartFile[] uploadFile, Model model) {

        String uploadFolder = "C:\\upload";

        for (MultipartFile multipartFile : uploadFile) {

            File saveFile = new File(uploadFolder, multipartFile.getOriginalFilename());

            try {
                multipartFile.transferTo(saveFile); // saveFile 위치에 저장
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/uploadAjax")
    public String uploadAjax() {

        return "uploadAjax";
    } //uploadAjax.jsp를 반환


    private String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();
        //오늘 날짜를 포맷팅해줌
        String str = sdf.format(date);

        return str.replace("-", File.separator);
    }

    private boolean checkImageType(File file) {

        try {
            String contentType = Files.probeContentType(file.toPath());

            //이미지로 시작하는 파일이면 true를 리턴
            return contentType.startsWith("image");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Ajax로 넘어옴 ( upload버튼을 눌렀을 때 )
    @PostMapping(value = "/uploadAjaxAction", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<AttachFileDTO>> uploadAjaxPost(MultipartFile[] uploadFile) {

        List<AttachFileDTO> list = new ArrayList<>();
        String uploadFolder = "C:\\upload";

        String uploadFolderPath = getFolder(); //위에 메소드 getFolder()호출

        File uploadPath = new File(uploadFolder, uploadFolderPath);

        //folder가 존재하지 않으면 생성해줌. mkdirs- 상위폴더까지 생성해줌
        if (uploadPath.exists() == false) {
            uploadPath.mkdirs();
        }
        for (MultipartFile multipartFile : uploadFile) {

            AttachFileDTO attachDTO = new AttachFileDTO();

            String uploadFileName = multipartFile.getOriginalFilename();

            uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);

            attachDTO.setFileName(uploadFileName);

            //랜덤으로 uuid 배정
            UUID uuid = UUID.randomUUID();

            uploadFileName = uuid.toString() + "_" + uploadFileName;

            try {
                File saveFile = new File(uploadPath, uploadFileName);
                multipartFile.transferTo(saveFile);

                attachDTO.setUuid(uuid.toString());
                attachDTO.setUploadPath(uploadFolderPath);

                if (checkImageType(saveFile)) {

                    attachDTO.setImage(true); //image는 boolean타입
                    //썸네일 이미지 표시 s_



                    Thumbnails.of(new File(uploadPath.getAbsolutePath() +"\\" + uploadFileName))
                            .size(100, 100)
                            .toFile(new File(uploadPath, "s_" + uploadFileName));

                    /*FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));

                    //썸네일 크기로 만들어줌
                    Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100, 100);

                    thumbnail.close();*/
                }
                list.add(attachDTO);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    //이미지를 눌렀을때 크게 보여주는 거 img src="/display"
    @GetMapping("/display")
    @ResponseBody
    public ResponseEntity<byte[]> getFile(String fileName) {

        File file = new File("c:\\upload\\" + fileName);

        ResponseEntity<byte[]> result = null;


        try {
            HttpHeaders headers = new HttpHeaders();

            headers.add("Content-Type", Files.probeContentType(file.toPath()));
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // a href='/download?fileName
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@RequestHeader("User-Agent") String userAgent, String fileName) {
//upload내 존재하는 fileName
// FileSystemResource -파일시스템의 특정파일로 부터 정보를 읽어온다.
        Resource resource = new FileSystemResource("C:\\upload\\" + fileName);
//resource가 존재하는가 - 없다 -> Not Found
        if (resource.exists() == false) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
//있다 fileName을 가져와서 resourceName에 넣어줌
        String resourceName = resource.getFilename();
//_ 이전의 유효 아이디를 지우고 / 원본이름을 가져옴 (추출)
        String resourceOriginalName = resourceName.substring(resourceName.indexOf("_") + 1);

        HttpHeaders headers = new HttpHeaders();

        try {

            String downloadName = null;
//header에 입력함 - download 이름으로 설정
//.contain - 대상 문자열에 특정 문자열이 포함되어 있는지 확인하는 함수

            if (userAgent.contains("Trident")) {
                downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8").replaceAll("\\+", "");
            } else if (userAgent.contains("Edge")) {
                downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8");
            } else {
                downloadName = new String(resourceOriginalName.getBytes("UTF-8"), "ISO-8859-1");
            }
//브라우저 - 헤더정보에 출력함.
            headers.add("Content-Disposition", "attachment; filename=" + downloadName);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
    }


    @PostMapping("/deleteFile")
    @ResponseBody
    //Ajax로 넘어오는 fileName, type
    public ResponseEntity<String> deleteFile(String fileName, String type) {

        File file;

        try {
            file = new File("c:\\upload\\" + URLDecoder.decode(fileName, "UTF-8"));
//파일 삭제
            file.delete();
//image인 경우 썸네일도 삭제할 수 있도록
            if (type.equals("image")) {
//썸네일을 없는걸로 바꿈 s_를 ""로 replace (원본과 이름이 같도록)
                String largeFileName = file.getAbsolutePath().replace("s_", "");

                file = new File(largeFileName);
//이미지 삭제
                file.delete();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<String>("deleted", HttpStatus.OK);

    }

}



