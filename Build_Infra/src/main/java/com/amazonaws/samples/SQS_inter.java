package com.amazonaws.samples;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.CreateQueueResult;

public interface SQS_inter {

	public void create_Queue(String queueName);

	public void delete_Message(Message message, String queueName);

	public Message receive_Message(String queueName, Integer waitTime, Integer visibilityTimeout);

	public void send_Message(String messageBody, String queueName, Integer delaySeconds);
	
	public Integer getApproximateNumberOfMsgs(String queueName);
	
	public DeepLearningResponse deepLearningOutput();
	
	//public Integer getApproximateNumberOfMsgs(String queueName);

}
