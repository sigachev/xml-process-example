package com.missioncritical.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.missioncritical.model.Message;
import com.missioncritical.model.Request;
import com.missioncritical.model.Response;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;


public class MessageHandler implements RequestHandler<Request, Response>{

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private final String XML_SCHEMA_BUCKET = "sigachev-new";
    private final String XML_SCHEMA_KEY = "schema.xsd";
    private final String QUEUE_URL = "your-sqs-queue-url";
    private final String SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:960163975060:errorsTopic";


    @Override
    public Response handleRequest(Request request, Context context) {
        Response  response = new Response();
        try {

            // Validate XML payload
            validateXml(request.getMessage());

            // Converting input payload to java object
/*            JAXBContext context = JAXBContext.newInstance(Message.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Message message = (Message) jaxbUnmarshaller.unmarshal(new StringReader(xmlPayload));

            // Store in database
            storeMessage(message);

            // Send to SQS queue
            sendToQueue(message);*/

            response.setStatusCode(200);
            response.setBody("Message processed successfully.");
            log.info("Message processed successfully.");
        } catch (
        ValidationException e) {
            response.setStatusCode(400);
            response.setBody("Invalid XML payload: " + e.getMessage());
            log.error("Error validating XML payload: " + e.getMessage());
            sendNotification("Error validating XML payload: " + e.getMessage());
        } catch (Exception e) {
            log.error("Internal Server Error: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("Internal Server Error: " + e.getMessage());
            sendNotification("Unexpected error: " + e.getMessage());
        }
        return response;
    }


    private void validateXml(String xmlPayload) throws JAXBException, SAXException, IOException {
        // Fetch XML schema from S3
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        String schema = s3Client.getObjectAsString(XML_SCHEMA_BUCKET, XML_SCHEMA_KEY);
        InputStream s3stream = s3Client.getObject(XML_SCHEMA_BUCKET, XML_SCHEMA_KEY).getObjectContent();


        Message message = new Message();

        //Get JAXBContext
        JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
        JAXBSource source = new JAXBSource(jaxbContext, message);

        //Create Unmarshaller
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        //Setup schema validator
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schemaObj = sf.newSchema(new URL("https://"+XML_SCHEMA_BUCKET+".s3.amazonaws.com/"+XML_SCHEMA_KEY));

        jaxbUnmarshaller.setSchema(schemaObj);


        // Validation logic
        // Throws ValidationException on failure
        try {
            Validator validator = schemaObj.newValidator();
            validator.validate(source);
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
        }
    }


    private void storeMessage(String xmlPayload) {
        // Use JDBC to store message in RDS
    }


/*
    private void sendToQueue(String xmlPayload) {
        // Send message to SQS queue
        AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(QUEUE_URL)
                .withMessageBody(xmlPayload);
        sqsClient.sendMessage(sendMessageRequest);
    }
*/

    private void sendNotification(String message) {
        // Send notification using AWS SNS
        AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
        PublishRequest publishRequest = new PublishRequest()
                .withTopicArn(SNS_TOPIC_ARN)
                .withSubject("Error in Microservice")
                .withMessage(message);
        snsClient.publish(publishRequest);
    }



}
