# AutoScaling in AWS without using default functionality

by Arsh Deep Singh Padda,  Rushikesh Sargar, Suhas Venkatesh Murthy
## 1. Architecture

![Image of Architecture](https://github.com/RishiSargar/autoscale_with_aws/blob/master/Architecture.JPG)

Different AWS Components used in the projects are the following:
### Amazon Elastic Compute Cloud (AWS EC2)
* EC2 is the main processing unit for both the web tier and app tier. In the web
tier, we are using an EC2 to handle the requests and send the message to SQS.
This EC2 is also responsible to deliver the response to all the incoming requests.
In the app tier, we areusing EC2 to do video retrieval and video recognition.
### Amazon Simple Queue Service (AWS SQS)
* We are using an SQS to maintain an input queue and an output queue. The role
of input queue is to generate a message id for each request and push it to queue
which will be later retrieved by EC2 for processing. The role of the output queue
is to deliver the response back to the request received.
### Amazon Simple Storage Service (AWS S3)
* We are using an S3 for storing the video name as the key and the result of the
deep learning model as value.
We had two architecture in our mind:
### Architecture 1
#### Web Tier Role
* Receive the request and download the video. Save the video into an S3
bucket (video bucket) with the key as the video name and the value as
the video. The web tier then sends a message in the queue with the video
file name as the message body. The web tier will then keep trying to
check another s3 (answer bucket) with the filename as the key to look for
answers. Once it receives the answer, it sends it back as a response in the
form <filename, result>.
#### Queue Role
* Receive the message from web tier and hold them till app tier is ready to
receive the message. This architecture doesn’t require an output queue.
#### App Tier Role
* Once the app tier EC2 instance receives a message from the queue, It
fetches the video from the s3 bucket (video bucket). It then runs the
video recognizing model and feteches the result. The results are then
stored in the s3 bucket (answer bucket). The instance terminate
themselves if there are no messages in the queue.
#### Problem with the Architecture:
* We were using two S3 buckets which was an overhead when we could
have achieved the desired result using only one S3 bucket.
* The continuous polling for looking into the answer bucket was
redundant.
* If we have concurrent requests then there is a lot of work for the web tier
to do. This can cause a bottleneck as the web tier has to download and
then push the file to S3.
* All the above reasons forced us to change the architecture.
### Architecture 2
#### Web Tier Role
* Receive the request and send a message in input queue to notify the EC2
that a request has been made. Listen for messages in the output queue.
The responses were sent to which every thread receives the message first
from the output queue. The web tier plays the role of scale out. It checks
the number of messages in the queue and the number of instances that
are turned on (this also includes the number of instances that are
pending i.e. in process of turning on).
#### Queue Role
* Receive the message from web tier and hold them till app tier is ready to
receive the message. Similarly, we have an output queue which will hold
the filename and result to the request posted once EC2 has done it’s
work.
#### App Tier Role
* Once the app tier EC2 instance receives a message from the queue, it
sends a request to the url and downloads the video. It then runs the
video recognizing model and feteches the result. The results are then
stored in the s3 bucket (answer bucket). The instance then delete the
video downloaded and sends the message to the output queue. The
instance will then keep looking in the queue for the next 30 seconds and
will start the process of recognition if it finds a message. If there is no
message then it will terminate itself. In order to serve request
immediately, we kept one instance on EC2 which will be always running.
The maximum number of instances that can be used at any time is 19
instances.
#### Fault Tolerance
* Our architecture is fault tolerance as once an EC2 instance will pick up a
message, then it will make the message invisible to others in the queue. If
the EC2 instances runs in some problem than the message will be
available back in the queue after 10000 seconds. If the message is
processed properly, then the message will be deleted. This ensures there
is no duplication.
## 2. Autoscaling
Explain in detail how your design and implementation support automatic scale-up and
-down on demand.
We made scale up the responsibility of the web tier since it’s main responsibility is to
handle the response and send them to the input queue. The number of messages in the queue
acts as a measure of how many instances we need to start in order to scale out. The web tier
will check the number of messages in queue and will check how many instances are running
(this includes the instances which are in processing stage also), if the difference of the number
is positive, then that many instances are turned on. If there are already 19 instances running,
then no action is taken. The App tier instances will perform their task and pick the next request
from the queue if there is any available.
Scale down was done by the EC2 instances. Once they process a request from the queue,
the instances look into the queue to see if there are any more requests. If there aren’t any
more requests, then the instance looks every second for the next 30 seconds and shutdown if
there are no requests. This 30 seconds delay was intentionally decided by the team because we
felt that this way we should be able to handle sudden recurring burst of requests more
efficiently.
## 3. Code
### 3.1 Code Modules
#### Web-Tier Module
* Web-Tier module is a spring boot app following MVC framework.
* We have a main thread which monitors the number of instances to be
created based on the number of requests to be served.
* Requests are served using different thread which hits the controller.
* When a request is made by the client, we add a message to the input SQS
for the app tier to process it.
* Web tier retrieves the message ID from the message sent to input SQS in
order to map client request and response from app tier.
* Web tier continuously monitors the output SQS for response. The
response message has a message body in this format -
“message-id:video-name:result”.
* Web tier maintains a HashTable to store messages since the web app tier
continuously polls the output SQS.
* If a client request’s result is available in the output SQS, web app polls it,
compares message ID and returns it to the client if the message ID of the
request and message ID in the response message body is same. If not, the
result is saved in the HashTable and returned from HashTable when that
request is being served.
* Web app deletes the message from the output queue once it polls it from
the SQS.
#### Load Balancer Module
* Scale out is implemented at the web tier. Scale out creates instances on
demand based on the number of running instances and number of
messages in the input SQS.
* We have two running instances at all times - Web tier and Main Listener.
Apart from these we can create a maximum of 18 more, summing up to
20 on a whole.
* Web tier creates instances using the Listener’s AMI, which when boots
up, picks up the message from the input SQS and puts the result to
output SQS.
* Web app tier’s primary thread continuously monitors the number of
messages in the input SQS and number of running instances at the
moment.
* If the number of messages to be served is greater than the number of
instances running and lesser than 20 (Limit), load balancer triggers a
request to create more instances with Listener’s AMI to serve the
requests.
* At most, we create 18 more instances. If there are more than 20
requests, load balancer triggers a request to create maximum allowed
number of instances which is 18 more and others requests have to wait
in the input SQS.
#### Listeners Module
* This module is mainly executed by the Listeners in App-Tier which has 2
type of Listeners : Main Listener and App Listeners
* All listeners will receive message containing the url from the input SQS i.e
input_queue and it will download the video (on EC2 listener) and pass the
video path to the deep learning module to predict the objects in the
video.
* Call for the deep learning module is made using process builder where
the path of the video and the weight is passed.
* Output of the deep learning module is pushed on s3 bucket with the file
name as the video_name and predicted value as the content of the file.
* The output is also passed to the output SQS i.e output_queue in the
format (input_message_id : video_name : predicted_output)
* Once the output is sent, the message from the input_queue is deleted.
* And App_Listener looks for available message to pick from the
input_queue, if it doesn’t receive any message, it waits for a small
amount of time for the next messages, if it doesn’t get any , it terminates
and performs the scale-in operation. t2.micro is a smaller instance and
latency of turning an instance on is more, to avoid that delay, the
Listeners wait before terminating.
* The Main Listener remains on all the time, and looks for messages in the
input queue.
#### Build Infrastructure
* This module builds the entire infrastructure for the project.
* AMI of Web_Tier_Image, Main_Listeners Image, Bucket Name, Queue
Names and other credentials are provided to the module
* A java code, creates the necessary components of the project i.e
Web_Tier, Main_Listener, Input_Queue and Output_Queue and S3
bucket
* And returns the ip of the web_tier which can be used to use the
application.
* The spring boot application takes 15secs to start the server, after which
the web tier is ready to take the requests.
### 3.2 Project Setup
#### Setup of Web-Tier
* Instruction given in the project description were followed to setup the
project, Set default region to us-west-1 and ami-0e355297545de2f82 to
create the app tier.
* Create a jar of the MainListener module and move it on the EC2 instance
using scp command on the local terminal i.e scp -i key_pem
<Web_Tier_file_name>.jar ubuntu@<public_ip>:/home/ubuntu/
* Log into the EC2 instance and change the permission of the jar file using:
chmod +x <Web_Tier_file_name>.jar
* Setup java environment by installing default-jre(jdk) on the instance
* To execute this jar file on boot, add the execution command in crontab
using : crontab -e , a file will be opened, press i to edit it and add “ java
-jar /home/ubuntu/<Web_Tier_file_name>.jar ” in it save and close the
file using :wq
* Create Image of this instance and mark it as Web_Tier_Image.
#### Setup of App-Tier
* Instruction given in the project description were followed to setup the
project, Set default region to us-west-1 and ami-0e355297545de2f82 to
create the app tier.
* Create a jar of the MainListener module and move it on the EC2 instance
using scp command on the local terminal i.e scp -i key_pem
<Main_Listener_file_name>.jar ubuntu@<public_ip>:/home/ubuntu/
* Log into the EC2 instance and change the permission of the jar file using:
chmod +x <Main_Listener_file_name>.jar
* Setup java environment by installing default-jre(jdk) on the instance
* To execute this jar file on boot, add the execution command in crontab
using : crontab -e , a file will be opened, press i to edit it and add “ java
-jar /home/ubuntu/<Main_Listener_file_name>.jar ” in it save and close
the file using :wq
* Create Image of this instance and mark it as MainListener_Image.
* Similarly, Create a jar file for the AppListeners and follow the same steps
to execute it on boot and create image of it as AppListener_Image.
### 3.3 Execution Steps
#### Execute the Build_Infra java code from any local machine, it will setup the
#### infrastructure and return the web_tie’s public ip.
#### You will be able to see Web-Tier and Main_Listener running on the console.
#### You can execute send_request.sh script to make requests to the web_tier,
#### console will print the output for each request. Syntax usage - 
#### ./send_request.sh
#### <url> <number_of_concurrent_requests> <total_number_of_requests>.
#### Scale Out and Scale in results can be observed in the console.
### 4. Project status
We met all the requirements of the projects which were the following:
* Web Tier is implemented successfully, which accepts the request and responds back
with the result of the deep learning model. We have only one EC2 instance which is
running this module.
* App Tier has been implemented successfully - It has 2 modules with one main listener
which is always in an ec2 instance. The other listener comes up based on demand. Web
tier scales up - triggers create request with Listener AMI to process requests. Further,
we also implemented the scale-in at app tier to shut-down instances if it is done
processing the request.
* Load Balancer module - we have implemented the scaling up successfully. The module
continuously monitors the input SQS and scales up accordingly to handle the requests
gracefully.
