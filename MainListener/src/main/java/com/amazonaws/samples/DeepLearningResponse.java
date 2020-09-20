package com.amazonaws.samples;

public class DeepLearningResponse implements DeepLearningResponse_Inter {
	String predictedValue;
	String videoName;
	
	DeepLearningResponse(String a,String b)
	{
		this.predictedValue=a;
		this.videoName=b;
	};
	
	DeepLearningResponse() {
	};

}
