package com.smart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class StorageConfig {

	//@Value("${cloud.aws.credentials.access-key}")
	@Value("${aws_access_key}")
	private String accessKey;
	
	//@Value("${cloud.aws.credentials.secret-key}")
	@Value("${aws_secret_key}")
	private String accessSecret;
	
	//@Value("${cloud.aws.region.static}")
	@Value("${aws_region_static}")
	private String region;
	
	@Bean
	@Profile("default")
	public AmazonS3 S3clientWithKeys() {
		//System.out.println(accessKey + " "+accessSecret);  // just to make sure if default prof is selected and if it is able to access keys
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
		return AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(region).build();
				
				
	}  
	
	//code - without access keys & only relying on IAM role
	@Bean
	@Profile("!default")
	public AmazonS3 S3clientWithoutKeys() {
		return AmazonS3ClientBuilder.standard().build();
	}
	
	
}
