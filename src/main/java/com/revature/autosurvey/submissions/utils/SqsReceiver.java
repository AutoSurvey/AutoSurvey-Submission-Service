package com.revature.autosurvey.submissions.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.autosurvey.submissions.beans.Response;
import com.revature.autosurvey.submissions.data.ResponseRepository;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

/**
 * @author igastelum
 *
 */

@Log4j2
@Component
public class SqsReceiver {
	public final static String qname = SQSNames.SUBMISSIONS_QUEUE;
	private String destQname = "https://sqs.us-east-1.amazonaws.com/855430746673/AnalyticsQueue";
	private Message<String> lastReceived;
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyy-MM-dd");

	public ResponseRepository repository;
	public ObjectMapper mapper;
	private SqsSender sqsSender;

	public SqsReceiver() {
		super();
	}

	public SqsSender getSqsSender() {
		return sqsSender;
	}

	@Autowired
	public void setSqsSender(SqsSender sqsSender) {
		this.sqsSender = sqsSender;
	}

	@Autowired
	public void setResponseRepo(ResponseRepository repository) {
		this.repository = repository;
	}

	@Autowired
	public void setObjectMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public String getDestQname() {
		return destQname;
	}

	public void setDestQname(String destQname) {
		this.destQname = destQname;
	}

	public Message<String> getLastReceived() {
		return lastReceived;
	}

	public void setLastReceived(Message<String> lastReceived) {
		this.lastReceived = lastReceived;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	@SqsListener(value = qname, deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
	public void queueListener(Message<String> message) {
		log.debug("Survey Queue listener invoked");
		System.out.println("Survey Queue listener invoked");

		log.debug("Headers received: {}", message.getHeaders());
		System.out.println("Headers received: " + message.getHeaders());
		String req_header = message.getHeaders().get("MessageId").toString();
		log.debug("Message ID Received: {}", req_header);
		System.out.println("Message ID Received: " + req_header);

		String payload = message.getPayload();
		log.debug("Payload received: ", payload);
		System.out.println("Payload received: " + payload);

		// Parse JSON payload and extract target survey ID from message
		String batch = "";
		String date = "";
		String surveyUuid = "";
		Flux<Response> res;
		Date startDate = null;

		try {
			JSONObject obj = new JSONObject(payload);
			System.out.println("Response batch received: " + obj.getString("batch"));
			batch = obj.getString("batch").equals("null") ? null : obj.getString("batch");
			System.out.println("Response date received: " + obj.getString("date"));
			date = obj.getString("date").equals("null") ? null : obj.getString("date");
			System.out.println("Response surveyUuid received: " + obj.getString("surveyUuid"));
			surveyUuid = obj.getString("surveyUuid").equals("null") ? null : obj.getString("surveyUuid");
		} catch (JSONException e1) {
			log.error(e1);
		}
		
		if(surveyUuid!=null) {
			res = repository.findAllBySurveyUuid(UUID.fromString(surveyUuid));
			sqsSender.sendResponse(res, UUID.fromString(req_header));
			return;
		}
		
		/*		
		if(!("").equals(batch) && !("").equals(date)) {
			try {
				startDate = dateTimeFormat.parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Calendar endCal = Calendar.getInstance();
    		endCal.setTime(startDate);
    		endCal.add(Calendar.DATE, 7);
    		Date endDate = endCal.getTime();
    		
    		res = repository.findAllByBatchAndWeek(batch, startDate, endDate);
    		log.trace("Response retrieved by Batch and Week");
    		System.out.println("Response retrieved by Batch and Week");
    		sqsSender.sendResponse(res, UUID.fromString(req_header));
    	}
    	*/
    	
    	if(!("").equals(batch)) {
    		res = repository.findAllByBatch(batch);
    		log.trace("Response retrieved by Batch name.");
    		System.out.println("Response retrieved by Batch name: " + batch);
    		sqsSender.sendResponse(res, UUID.fromString(req_header));
    		return;
    	}
    	
    	if(!("").equals(date)) {
			try {
				startDate = dateTimeFormat.parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		Calendar endCal = Calendar.getInstance();
    		endCal.setTime(startDate);
    		endCal.add(Calendar.DATE, 7);
    		Date endDate = endCal.getTime();
    		
    		res = repository.findAllByWeek(startDate, endDate);
    		log.trace("Response retrieved by Week");
    		System.out.println("Response retrieved by Week");
    		sqsSender.sendResponse(res, UUID.fromString(req_header));
    	}
    	
    	/*
    	{
    		  "uuid": null,
    		  "batch": null,
    		  "date": null,
    		  "surveyUuid": "12345678-1234-1234-1234-123456789abc",
    		  "responses": null
    		}
    	 */
		
	}
}