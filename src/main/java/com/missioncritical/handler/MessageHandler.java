package com.missioncritical.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.missioncritical.model.Message;
import com.missioncritical.model.Request;
import com.missioncritical.model.Response;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;


public class MessageHandler implements RequestHandler<Request, Response> {

    static LambdaLogger logger;
    private final String XML_SCHEMA_BUCKET = "sigachev-new";
    private final String XML_SCHEMA_KEY = "schema.xsd";
    private final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/960163975060/processedMessages";
    private final String SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:960163975060:errorsTopic";


    @Override
    public Response handleRequest(Request request, Context context) {
        logger = context.getLogger();

        Response response = new Response();

        try {
            String xml = request.getMessage();

            if (StringUtils.isBlank(xml)) {
                throw new IOException("Input is blank.");
            }


            // Validate XML payload
            validateXml(xml);


            //Converting input payload to java object
/*            JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            logger.log("1");
            Message message = (Message) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            logger.log("Unmarshalled message: " + message, LogLevel.DEBUG);*/

            // Store in database
            storeMessage(xml);

            // Send to SQS queue
            sendToQueue(xml);

            response.setStatusCode(200);
            response.setBody("Message processed successfully.");
            logger.log("Message processed successfully.", LogLevel.INFO);
        } catch (IOException e) {
            response.setStatusCode(400);
            response.setBody("Input is blank.");
            logger.log("Input is blank.", LogLevel.ERROR);
            sendNotification("Input is blank.");
        } catch (ValidationException e) {
            response.setStatusCode(422);
            response.setBody("Error validating XML payload: " + e.getMessage());
            logger.log("Error validating XML payload: " + e.getMessage(), LogLevel.ERROR);
            sendNotification("Error validating XML payload: " + e.getMessage());
        } catch (Exception e) {
            logger.log("Internal Server Error: " + e.getMessage(), LogLevel.ERROR);
            response.setStatusCode(500);
            response.setBody("Internal Server Error: " + e.getMessage());
            sendNotification("Unexpected error: " + e.getMessage());
        }
        return response;
    }


    private void validateXml(String xmlPayload) throws JAXBException, SAXException {
        // Fetch XML schema from S3
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InputStream s3stream = s3Client.getObject(XML_SCHEMA_BUCKET, XML_SCHEMA_KEY).getObjectContent();

        //Get JAXBContext
        JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);

        //Create Unmarshaller
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        //Setup schema validator
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schemaObj = sf.newSchema(new StreamSource(s3stream));
        jaxbUnmarshaller.setSchema(schemaObj);

        // Validation logic
        // Throws ValidationException on failure
        try {
            Validator validator = schemaObj.newValidator();
            StringReader reader = new StringReader(xmlPayload);
            validator.validate(new StreamSource(reader));
        } catch (IOException | SAXException e) {
            logger.log("Validation exception: " + e.getMessage(), LogLevel.ERROR);
            throw new ValidationException(e.getMessage());
        }
    }


    private void storeMessage(String xmlPayload) {
        // Use JDBC to store message in RDS
    }


    private void sendToQueue(String xmlPayload) {
        // Send message to SQS queue
        AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(QUEUE_URL)
                .withMessageBody(xmlPayload);
        sqsClient.sendMessage(sendMessageRequest);
    }


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
