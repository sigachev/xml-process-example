package com.missioncritical.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
//JAXB annotations
@XmlRootElement(name = "ExercisePayload")
@XmlAccessorType (XmlAccessType.FIELD)
//Jackson annotation
@JsonRootName(value = "ExercisePayload")
public class Message {

    //@XmlElement(name = "MessageID", required = true)
    //@JacksonXmlProperty(localName = "MessageID")
    String messageID;

    //@XmlElement(name = "MessageSentDateTime", required = true)
    //@JacksonXmlProperty( localName = "MessageSentDateTime")
    String messageSentDateTime;

    //@XmlElement(name = "SubjectName", required = true)
    //@JacksonXmlProperty(localName = "SubjectName")
    String subjectName;

    //@XmlElement(name = "SubjectEyeColor", required = true)
    //@JacksonXmlProperty(localName = "SubjectEyeColor")
    String subjectEyeColor;


}
