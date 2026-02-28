package com.kh.spring.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ChatFileUtil {
	
	@Autowired
	private ThumbnailGenerator thumbnailGenerator;
	
	@Value("${file.upload.path}")
	private String savePath;
	
	public String saveFile(MultipartFile uploadFile, String subFolder) throws Exception {
		
		//원본 파일명
		String originName = uploadFile.getOriginalFilename();
		
		//시간형식
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		
		//랜덤값 5자리 추출
		int ranNum = (int)(Math.random()*90000+10000);
		
		//확장자 추출
		String ext = originName.substring(originName.lastIndexOf("."));
		
		//합쳐주기 (타임스탬프 + 난수 + _ + 원본파일명)
		String changeName =  currentTime + ranNum + "_" + originName;
		
		// 저장 폴더 경로 설정 (기본 경로 + 하위 폴더)
		String folderPath = savePath;
		if (subFolder != null && !subFolder.isEmpty()) {
			folderPath += subFolder + "/";
		}
		
		File folder = new File(folderPath);
		// 폴더가 없으면 생성
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		//서버에 업로드 처리
		File destination = new File(folderPath + changeName);
		uploadFile.transferTo(destination);
		
		// 이미지 파일인 경우 썸네일 생성 (300x300) - 비동기 처리
		if (isImageFile(ext)) {
			thumbnailGenerator.generateThumbnail(destination, folderPath, changeName);
		}
		
		return changeName;
	}
	
	// 이미지 확장자 판별
	private boolean isImageFile(String ext) {
		String lowerExt = ext.toLowerCase();
		return lowerExt.equals(".jpg") || lowerExt.equals(".jpeg") || 
			   lowerExt.equals(".png") || lowerExt.equals(".gif") || 
			   lowerExt.equals(".bmp");
	}

	//첨부파일 삭제
	public boolean deleteFile(String deleteFile, String subFolder) {
		
		String folderPath = savePath;
		if (subFolder != null && !subFolder.isEmpty()) {
			folderPath += subFolder + "/";
		}
		
		//저장경로 + 저장파일명 
		String filePath = folderPath + deleteFile;
		
		File file = new File(filePath);
		
		if(file.exists()) {
			return file.delete();
		}
		
		return false;
	}
}
