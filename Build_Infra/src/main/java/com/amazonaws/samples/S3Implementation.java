package com.amazonaws.samples;
import com.amazonaws.samples.Values;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Get an object within an Amazon S3 bucket.
 *
 * This code expects that you have AWS credentials set up per:
 * http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
 */
public class S3Implementation implements S3_inter
{
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Values.REGION).withCredentials(
			new AWSStaticCredentialsProvider(new BasicAWSCredentials(Values.ACCESS_KEY, Values.SECRET_KEY)))
			.build();
	final String bucket_name=Values.BUCKETNAME;
	final String bucket_name_ans=Values.BUCKETNAME_ANS;
    /* (non-Javadoc)
	 * @see com.amazonaws.samples.S3_inter#getObject(java.lang.String, java.lang.String)
	 */
    @Override
	public void getObject(String bucket_name,String key_name)
    {
    	LOGGER.info("Getting object from s3");
        System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
        
        try {
            S3Object o = s3.getObject(bucket_name, key_name);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File(key_name));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
 
    }
   
    /* (non-Javadoc)
	 * @see com.amazonaws.samples.S3_inter#create_Bucket()
	 */
    @Override
	public Bucket create_Bucket(String name) {
    	
		LOGGER.info("Creating the bucket");
		Bucket b = null;
		if (s3.doesBucketExistV2(name)) {
			LOGGER.info("The bucket exits, so returning the existing bucket.");
			b = getBucket();
		} else {
			LOGGER.info("Creating a new bucket "+name);
			b = s3.createBucket(name);
		}

		return b;
	}
    
    public void get(String videoName) {
        LOGGER.info("Get object from S3!");
        //createBucket(bucketName);
        try {
        	S3Object object = s3.getObject(new GetObjectRequest(bucket_name, videoName));
        	InputStream reader = new BufferedInputStream(
        			   object.getObjectContent());
        			File file = new File("/home/ubuntu/"+videoName);      
        			OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));

        			int read = -1;

        			while ( ( read = reader.read() ) != -1 ) {
        			    writer.write(read);
        			}

        			writer.flush();
        			writer.close();
        			reader.close();
        } catch (Exception ex) {
            
        }
    }

    
    /* (non-Javadoc)
	 * @see com.amazonaws.samples.S3_inter#getBucket()
	 */
    @Override
	public Bucket getBucket() {
		LOGGER.info("Returning the required bucket.");
		Bucket Bucketname = null;
		List<Bucket> buckets = s3.listBuckets();
		for (Bucket b : buckets) {
			if (b.getName().equals(bucket_name)) {
				Bucketname = b;
			}
		}

		return Bucketname;
	}
    
    /* (non-Javadoc)
	 * @see com.amazonaws.samples.S3_inter#insertObject(java.lang.String, java.lang.String)
	 */
    @Override
	public void insertObject(String key, String value) {
		LOGGER.info("Inserting a object into the bucket.");
		this.create_Bucket(bucket_name);
		/*byte[] contentAsBytes = null;
		try {
			contentAsBytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//InputStream stream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
		//ByteArrayInputStream contentsAsStream = new ByteArrayInputStream(contentAsBytes);
		//ObjectMetadata md = new ObjectMetadata();
		//md.setContentLength(contentAsBytes.length);
		s3.putObject(bucket_name_ans, key, value);
	}
}
