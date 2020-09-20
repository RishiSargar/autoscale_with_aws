package com.amazonaws.samples;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.amazonaws.services.sqs.model.Message;

public class MainListener {
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private SQS_inter SQSi=new SQSImplementation();
	private S3_inter S3i=new S3Implementation();
	FileHandler fh;
	public void pick_predict_send() {
		try {
		fh = new FileHandler("Logs.log",true);  
        LOGGER.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
	} catch (SecurityException e) {  
        e.printStackTrace();  
    } catch (IOException e) {  
        e.printStackTrace();  
    }  
		LOGGER.info("MainListener and sending");
		while (true) {
			Message message = SQSi.receive_Message(Values.INPUTQUEUENAME, 1, 10000);
			if (message == null) {
				continue;
			}
			
			DeepLearningResponse DLResponse = SQSi.deepLearningOutput();
			if (DLResponse.predictedValue == null) {
				DLResponse.predictedValue = "NoPrediction";
			}
			System.out.println(DLResponse.videoName+":"+DLResponse.predictedValue);
			DLResponse.predictedValue = DLResponse.predictedValue.trim();
			String messageId = message.getMessageId();
			S3i.insertObject(DLResponse.videoName,DLResponse.videoName+":"+DLResponse.predictedValue);
			// Write in Output SQS and then delete
			SQSi.send_Message(messageId+":"+DLResponse.videoName+":"+DLResponse.predictedValue, Values.OUTPUTQUEUE, 0);
			SQSi.delete_Message(message, Values.INPUTQUEUENAME);
		}
	}
	public static void main(String [] args)
	{
		MainListener m= new MainListener();
		m.pick_predict_send();
	}

}
