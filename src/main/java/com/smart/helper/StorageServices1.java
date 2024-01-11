package com.smart.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

//@Service
public class StorageServices1 {
	//@Value("${application.bucket.name}")
	private String bucketname;
	
	public boolean upload(MultipartFile file, AmazonS3 s3client) {
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
	
	public byte[] getFile(String filename, AmazonS3 s3client) {
		S3Object s3Object = s3client.getObject(filename, filename);
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		try {
			byte[] content = IOUtils.toByteArray(inputStream);
			return content;
			
		}catch(Exception e) {
			System.out.println("Something went wrong");
		}
		return null;
	}
	
	public void deleteFile(String filename, AmazonS3 s3client) {
		s3client.deleteObject(bucketname, filename);
	}
}
