package com.amazonaws.samples;

import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.services.s3.model.Bucket;

public class Infra_Build {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final String QUEUE_NAME_IN = Values.INPUTQUEUENAME;
	private static final String QUEUE_NAME_OUT = Values.OUTPUTQUEUE;
	private static final String WEB_APP_IMAGE = Values.WEB_IMAGE;
	private static final String MAIN_APP_IMAGE = Values.MAIN_IMAGE;
	private static final String BUCKET_NAME = Values.BUCKETNAME;
	private static final String REGION = Values.REGION;
	private static SQS_inter SQSi=new SQSImplementation();
	private static S3_inter S3i=new S3Implementation();
	private static EC2_inter EC2i=new EC2Implementation();
	
	public static void main(String[] args)
	{
	Bucket b= S3i.create_Bucket(BUCKET_NAME);
	SQSi.create_Queue(QUEUE_NAME_IN);
	SQSi.create_Queue(QUEUE_NAME_OUT);
	List<String> web_dns=EC2i.createinstance(WEB_APP_IMAGE,1,1,"Web_Tier_Instance");
	List<String> ec2_dns=EC2i.createinstance(MAIN_APP_IMAGE,1,1,"Main_Listener_Instance");
	System.out.println("***************************************************************");
	System.out.println("Input Queue has been created");
	System.out.println("Output Queue has been created");
	System.out.println("Check the s3 result on : https://s3.console.aws.amazon.com/s3/buckets/"+BUCKET_NAME+"/?region="+REGION);
	System.out.println("Main Listener is running with ip : "+ec2_dns.get(0));
	System.out.println("***************************************************************");
	System.out.println("Web_Tier is running, hit : '"+web_dns.get(0)+":8080/videorecog' to run the app");
	System.out.println("***************************************************************");
	}

}
