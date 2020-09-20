package com.amazonaws.samples;

import java.util.logging.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.samples.Values;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;

public class EC2Implementation extends Thread implements EC2_inter{
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(Values.REGION).withCredentials(
			new AWSStaticCredentialsProvider(new BasicAWSCredentials(Values.ACCESS_KEY, Values.SECRET_KEY)))
			.build();
	

   
	public List<String> createinstance(String imageId, Integer maxNumberOfInstances, Integer nameCount, String Name) {

        
    	LOGGER.info("Creating an instance");
        

        //String imageId = "ami-0e355297545de2f82";  //image id of the instance
        int minInstanceCount = 1; //create 1 instance
        int maxInstanceCount = maxNumberOfInstances;
        
        Collection<TagSpecification> tagSpecifications = new ArrayList<TagSpecification>();
		TagSpecification tagSpecification = new TagSpecification();
		Collection<Tag> tags = new ArrayList<Tag>();
		Tag t = new Tag();
		t.setKey("Name");
		t.setValue(Name);
		tags.add(t);
		tagSpecification.setResourceType("instance");
		tagSpecification.setTags(tags);
		tagSpecifications.add(tagSpecification);

        RunInstancesRequest rir = new RunInstancesRequest(imageId,
                minInstanceCount, maxInstanceCount);
        rir.setInstanceType(Values.INSTANCE_TYPE); //set instance type
        rir.setKeyName(Values.KEY_NAME);
        rir.setTagSpecifications(tagSpecifications);
        rir.withSecurityGroupIds(Values.SECURITY_GROUP);
        

        RunInstancesResult result = ec2.runInstances(rir);

        List<Instance> resultInstance =
                result.getReservation().getInstances();
        List<String> list_dns= new ArrayList<String>();
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("fata");
		}

        for(Instance ins : resultInstance) {
            LOGGER.info("New instance has been created:" +
                    ins.getInstanceId());//print the instance ID
            //System.out.println(getInstancePublicDnsName(ins.getInstanceId()));
            list_dns.add(getInstancePublicDnsName(ins.getInstanceId()));
        
           
        }
        
        return list_dns;
    }
	public String getInstancePublicDnsName(String instanceId) {
	    DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
	    List<Reservation> reservations = describeInstancesRequest.getReservations();
	    Set<Instance> allInstances = new HashSet<Instance>();
	    for (Reservation reservation : reservations) {
	      for (Instance instance : reservation.getInstances()) {
	        if (instance.getInstanceId().equals(instanceId))
	          return instance.getPublicIpAddress();
	      }
	    }
	    return null;
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

