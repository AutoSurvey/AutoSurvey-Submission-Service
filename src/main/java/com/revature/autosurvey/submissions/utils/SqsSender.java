package com.revature.autosurvey.submissions.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.revature.autosurvey.submissions.beans.Response;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

/**
 * @author Jasmine
 *
 */

@Log4j2
@Data
@Component
public class SqsSender {

	private final QueueMessagingTemplate queueMessagingTemplate;
	private String queueName = SQSNames.SUBMISSIONS_QUEUE;
	private AmazonSQS sqsExtended;

	@Autowired
	@Qualifier("AmazonSQS")
	public void setAmazonSQS(AmazonSQS sqsExt) {
		sqsExtended = sqsExt;
	}
	
	@Autowired
	public SqsSender(AmazonSQSAsync sqs) {
		this.queueMessagingTemplate = new QueueMessagingTemplate(sqs);
	}

	public void sendResponse(Flux<Response> response, UUID id) {
		log.trace("Response received by Sender");
		System.out.println("Response received by Sender");
		List<Response> list = new ArrayList<Response>();
		response.map(r -> {
			list.add(r);
			return r;
		}).blockLast();
		System.out.println("Message to be sent: " + list);

		// Build response from list and send to Analytics Service
		Message<String> message = MessageBuilder.withPayload(list.toString())
				.setHeader("MessageId", id.toString())
				.build();
//		queueMessagingTemplate.send(queueName, message);
		
		try {
			queueMessagingTemplate.send(this.queueName, message);
			log.trace("Message sent." + list);
			
			System.out.println("Message sent: " + list);
		}	catch (Exception e) {
			log.error("Payload too large. Posting message to S3 instead: " + e);
		}
		
		// Send Message to S3 instead
	    // Create a message queue for this example.
	    GetQueueUrlResult qUrl = null;
	    String qUrlString = "";
	    String qName = "AnalyticsQueue";
	    try {
	    	qUrl = sqsExtended.getQueueUrl(qName);
	    } catch (Exception e) {
	    	log.warn("Attempt to get existing queue URL failed\nIt may not exist");
	    }
	    
	    if(!("").equals(qUrlString))
	    	qUrlString = sqsExtended.getQueueUrl(qName).toString();
	    else {		
	    	// Queue doesn't exist, create new one
		    final CreateQueueRequest createQueueRequest =
		            new CreateQueueRequest(qName);
		    qUrlString = sqsExtended
		            .createQueue(createQueueRequest).getQueueUrl();
		    System.out.println("Queue created.");

	    }
	    
	    System.out.println("QueueUrl retrieved: " + qUrlString);
	    // Send the message.
	    sqsExtended.sendMessage(qUrlString, list.toString());
//	    final SendMessageRequest msgOverload =
//	            new SendMessageRequest(qUrl, list.toString());
//	    sqsExtended.sendMessage(msgOverload);
	    System.out.println("Sent the message: " + list);
	}
}
