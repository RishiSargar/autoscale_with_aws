package com.amazonaws.samples;

import java.util.logging.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.samples.Values;
import java.util.List;

import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;

public class EC2Implementation implements EC2_inter{
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(Values.REGION).withCredentials(
			new AWSStaticCredentialsProvider(new BasicAWSCredentials(Values.ACCESS_KEY, Values.SECRET_KEY)))
			.build();
	

   
	public Integer createinstance(String imageId, Integer maxNumberOfInstances, Integer nameCount) {

        
    	LOGGER.info("Creating an instance");
        System.out.println("create an instance");

        //String imageId = "ami-0e355297545de2f82";  //image id of the instance
        int minInstanceCount = 1; //create 1 instance
        int maxInstanceCount = maxNumberOfInstances;

        RunInstancesRequest rir = new RunInstancesRequest(imageId,
                minInstanceCount, maxInstanceCount);
        rir.setInstanceType(Values.INSTANCE_TYPE); //set instance type
        rir.setKeyName(Values.KEY_NAME);

        RunInstancesResult result = ec2.runInstances(rir);

        List<Instance> resultInstance =
                result.getReservation().getInstances();

        for(Instance ins : resultInstance) {
            System.out.println("New instance has been created:" +
                    ins.getInstanceId());//print the instance ID
            
           
        }
        return nameCount;
    }

   
	public void startinstance(String instanceId) {
    	LOGGER.info("Starting the instance "+instanceId);
        StartInstancesRequest request = new StartInstancesRequest().
                withInstanceIds(instanceId);//start instance using the instance id
        ec2.startInstances(request);

    }

    
	public void stopinstance(String instanceId) {
    	LOGGER.info("Stopping the instance "+instanceId);
        StopInstancesRequest request = new StopInstancesRequest().
                withInstanceIds(instanceId);//stop instance using the instance id
        ec2.stopInstances(request);

    }
    
    
	public void terminateinstance() {
    	String myId = EC2MetadataUtils.getInstanceId();
    	LOGGER.info("Terminating the instance "+myId);
        TerminateInstancesRequest request = new TerminateInstancesRequest().
                withInstanceIds(myId);//terminate instance using the instance id
        ec2.terminateInstances(request);

    }
    
    
    
	public DescribeInstanceStatusResult describeInstance(DescribeInstanceStatusRequest describeRequest) {
    	//final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		return ec2.describeInstanceStatus(describeRequest);
	}
    
    public Integer getNumberOfInstances() {
		// TODO Auto-generated method stub
		DescribeInstanceStatusRequest describeRequest = new DescribeInstanceStatusRequest();
		describeRequest.setIncludeAllInstances(true);
		DescribeInstanceStatusResult describeInstances = describeInstance(describeRequest);
		List<InstanceStatus> instanceStatusList = describeInstances.getInstanceStatuses();
		Integer countOfRunningInstances = 0;
		for (InstanceStatus instanceStatus : instanceStatusList) {
			InstanceState instanceState = instanceStatus.getInstanceState();
			if (instanceState.getName().equals(InstanceStateName.Running.toString())) {
				countOfRunningInstances++;
			}
		}
		
		return countOfRunningInstances;
	}
    /*
    public static void main(String [] args)
    {
    	final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

    	DescribeKeyPairsResult response = ec2.describeKeyPairs();

    	for(KeyPairInfo key_pair : response.getKeyPairs()) {
    	    System.out.printf(
    	        "Found key pair with name %s " +
    	        "and fingerprint %s",
    	        key_pair.getKeyName(),
    	        key_pair.getKeyFingerprint());
    	}
    }
    */
    
  



}

