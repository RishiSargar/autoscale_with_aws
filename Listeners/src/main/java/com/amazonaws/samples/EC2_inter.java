package com.amazonaws.samples;

import java.util.List;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;

public interface EC2_inter {

	Integer createinstance(String imageId, Integer maxNumberOfInstances, Integer nameCount);

	void startinstance(String instanceId);

	void stopinstance(String instanceId);

	void terminateinstance();

	DescribeInstanceStatusResult describeInstance(DescribeInstanceStatusRequest describeRequest);
	
	public Integer getNumberOfInstances();

}