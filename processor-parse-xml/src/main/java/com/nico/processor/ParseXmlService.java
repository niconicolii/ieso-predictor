package com.nico.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nico.processor.dataClasses.DemandData;
import com.nico.processor.dataClasses.DemandMultidaysData;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.cglib.core.Local;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParseXmlService {
    // the latest time sent to store in db
    private LocalDateTime maxDateTime = LocalDateTime.MIN;

    private final ObjectMapper objectMapper;

    public ParseXmlService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }


    public DemandMultidaysData parseXmlToObj(String xmlString) throws Exception {
        JAXBContext context = JAXBContext.newInstance(DemandMultidaysData.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlString);

        DemandMultidaysData dmd = (DemandMultidaysData) unmarshaller.unmarshal(reader);
        return dmd;
    }

    public Message<String> demandDataToMessage(DemandData data) throws IOException {
        String json = objectMapper.writeValueAsString(data);
//        System.out.println("===========================\n" + json);
        return MessageBuilder.withPayload(json).build();

    }

    public List<Message<String>> getMessageList(DemandMultidaysData dmd) throws IOException {
        List<Message<String>> messages = new ArrayList<>();

        List<DemandData> datas = dmd.getDataSet().getDatas();
        LocalDateTime startDateTime = dmd.getStartDate();
        for (int i = 0; i < datas.size(); i++) {
                LocalDateTime currDateTime = startDateTime.plusMinutes(5L * i);
                if (currDateTime.isAfter(maxDateTime)) {
                    datas.get(i).setTimestamp(currDateTime);
//                    String payload = serializeDemandData(datas.get(i));
                    messages.add(
                            demandDataToMessage(datas.get(i))
                    );
                    maxDateTime = currDateTime;
                }
        }
        return messages;
    }
}
