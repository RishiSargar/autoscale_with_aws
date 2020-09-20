package com.amazonaws.samples;

import com.amazonaws.services.s3.model.Bucket;

public interface S3_inter {

	void getObject(String bucket_name, String key_name);

	Bucket create_Bucket(String name);

	Bucket getBucket();

	void insertObject(String key, String value);
	
	void get(String videoName);

}