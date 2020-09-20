package com.amazonaws.samples;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import java.io.File;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;


import java.net.URLConnection;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SQSImplementation implements SQS_inter
{	
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String QUEUE_NAME = Values.INPUTQUEUENAME;
    private final DeepLearningResponse DLResponse=new DeepLearningResponse();
    final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Values.REGION).withCredentials(
			new AWSStaticCredentialsProvider(new BasicAWSCredentials(Values.ACCESS_KEY, Values.SECRET_KEY)))
			.build();
    String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
    private S3_inter S3i=new S3Implementation();
    FileHandler fh;
    
    @Override
    public CreateQueueResult create_Queue(String queueName) {
		LOGGER.info("Creating a queue.");
		CreateQueueResult createQueueResult = sqs.createQueue(queueName);
		return createQueueResult;
	}
    
    public Message receive_Message(String queueName, Integer waitTime, Integer visibilityTimeout) {
    	//Integer numOfMsgs = getApproximateNumberOfMsgs(Values.INPUTQUEUENAME);
		LOGGER.info("Receiving a message from the queue");
		String queueUrl_internal = sqs.getQueueUrl(queueName).getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl_internal);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		receiveMessageRequest.setWaitTimeSeconds(waitTime);
		receiveMessageRequest.setVisibilityTimeout(visibilityTimeout);
		ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(receiveMessageRequest);
		List<Message> messageList = receiveMessageResult.getMessages();
		if (messageList.isEmpty()) {
			return null;
		}
		return messageList.get(0);
	}
    
    
    public void send_Message(String message, String queueName, Integer delaySeconds) {
		LOGGER.info("Sending a message in a queue");
		String queueUrl_internal = null;
		try {
			queueUrl_internal = sqs.getQueueUrl(queueName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			CreateQueueResult createQueueResult = this.create_Queue(queueName);
		}
		queueUrl_internal = sqs.getQueueUrl(queueName).getQueueUrl();
		SendMessageRequest sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl_internal)
				.withMessageBody(message).withDelaySeconds(delaySeconds);
		sqs.sendMessage(sendMessageRequest);

	}
    
    public void delete_Message(Message message, String queue)
    {
    	LOGGER.info("Deleting message from the queue");
    	
    	String messageReceiptHandle = message.getReceiptHandle();
		DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, messageReceiptHandle);
		sqs.deleteMessage(deleteMessageRequest);
    	
    	/*
        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
        System.out.println(messages.size());
        
        for (Message m : messages) {
        	System.out.println(m);
            sqs.deleteMessage(queueUrl, m.getReceiptHandle());
        }*/
    	
    }
    

    public Integer getApproximateNumberOfMsgs(String queueName) {
		LOGGER.info("Getting approximate number of messages.");
		String queueUrl = null;
		try {
			queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
		} catch (QueueDoesNotExistException queueDoesNotExistException) {
			CreateQueueResult createQueueResult = this.create_Queue(queueName);
		}
		queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessages");
		GetQueueAttributesRequest getQueueAttributesRequest = new GetQueueAttributesRequest(queueUrl, attributeNames);
		Map map = sqs.getQueueAttributes(getQueueAttributesRequest).getAttributes();
		String numberOfMessagesString = (String) map.get("ApproximateNumberOfMessages");
		Integer numberOfMessages = Integer.valueOf(numberOfMessagesString);
		return numberOfMessages;
	}
    
    public File downloadFile(File dir) {

        File download = null;

        try {

            URL url = new URL("http://206.207.50.7/getvideo");
            URLConnection con = (URLConnection) url.openConnection();

            String fieldValue = con.getHeaderField("Content-Disposition");

            String filename = fieldValue.substring(fieldValue.indexOf("filename=") + 9, fieldValue.length());
            download = new File(dir, filename);

            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            FileOutputStream fos = new FileOutputStream(download);
            try {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } finally {
                fos.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return download;
    }
    public DeepLearningResponse deepLearningOutput() {
    	
    	File videoFile=null;
    	LOGGER.info("Running the deep learning model on");
    	UUID uuid = UUID.randomUUID();
    	File directory = new File(uuid.toString());
    	directory.mkdir();
    	LOGGER.info("Directory created"+directory.getAbsolutePath());
      try {
          videoFile = downloadFile(directory);
      } catch (Exception ex) {
          ex.printStackTrace();
          }
		String termOutput = null;
		String folder_path=directory.getAbsolutePath();
		//System.out.println(folder_path);
		Process p,p1;
		String video_path=videoFile.getAbsolutePath();
		String video_name=videoFile.getName();
		System.out.print(video_path);
		try {
			//
			LOGGER.info("Running darknet");
			p = new ProcessBuilder("bash", "-c","Xvfb :1 & export DISPLAY=:1 ; ./darknet detector demo cfg/coco.data cfg/yolov3-tiny.cfg yolov3-tiny.weights "+ video_path
					+ " -dont_show > result ; python darknet_test.py ;  cat result_label").start();
			p.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			LOGGER.info("Finished darknet");

			termOutput = br.readLine().trim();
			br.close();
			
			p.destroy();
			/*
			p1 = new ProcessBuilder("bash", "-c","rm -rf "+folder_path).start();
			p1.waitFor();
			p1.destroy();*/
			
			
		} catch (Exception e) {
			System.out.println(e);
			LOGGER.severe(e.toString());
		}
		finally {
			videoFile.delete();
			directory.delete();
		}
		return new DeepLearningResponse(termOutput, video_name);
		//return new DeepLearningResponse("DL_Value", "Video_Name");
	}
    
    
	
    
    public static void main(String[] args)
    {
    	SQSImplementation a= new SQSImplementation();
    	System.out.println(a.getApproximateNumberOfMsgs(QUEUE_NAME));
    	
    }
}
