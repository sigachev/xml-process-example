package com.missioncritical.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@AllArgsConstructor
@NoArgsConstructor
//JAXB annotations
@XmlRootElement(name = "ExercisePayload")
public class Message {

    //@XmlElement(name = "MessageID")
    String messageID;

    //@XmlElement(name = "MessageSentDateTime")
    String messageSentDateTime;

    //@XmlElement(name = "SubjectName")
    String subjectName;

    //@XmlElement(name = "SubjectEyeColor")
    String subjectEyeColor;

}
