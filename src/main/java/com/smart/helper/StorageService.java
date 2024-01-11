package com.smart.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

@Service
public class StorageService {

	@Value("${application.bucket.name}")
	private String bucketname;
	
	/*@Autowired
	private AmazonS3 s3client;*/
	
	//New config - not using access key
	private final AmazonS3 s3client;
	
	//private String bucketname="imagestoring-scm";
    @Autowired
    public StorageService(AmazonS3 s3client) {
        this.s3client = s3client;
    }
	
	public boolean upload(MultipartFile file) {
		try {
			File fileobj = convertMultipartFileToFile(file);
			String filename = file.getOriginalFilename();
			s3client.putObject(new PutObjectRequest(bucketname, filename, fileobj));
			fileobj.delete();
			return true;
		}catch(Exception e) {
			System.out.println("Something went wrong");
			return false;
		}
	}
	
	private File convertMultipartFileToFile(MultipartFile file) {
		File convertedfile = new File(file.getOriginalFilename());
		try(FileOutputStream fos = new FileOutputStream(convertedfile)) {
			fos.write(file.getBytes());
		}catch(IOException e) {
			System.out.println("Error converting... use s4lj log to check logs");
		}
		return convertedfile;
	}
	
	public byte[] getFile(String filename) {
		S3Object s3object = s3client.getObject(bucketname, filename);
		S3ObjectInputStream inputstream = s3object.getObjectContent();
		try {
			byte[] content = IOUtils.toByteArray(inputstream);
			return content;
			
		}catch(Exception e) {
			System.out.println("Something went wrong");
		}
		return null;
	}
	
	public void deleteFile(String filename) {
		s3client.deleteObject(bucketname, filename);
	}
}
