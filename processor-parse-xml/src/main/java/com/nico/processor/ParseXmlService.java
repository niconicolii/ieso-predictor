package com.nico.processor;

import com.nico.processor.dataClasses.DemandData;
import com.nico.processor.dataClasses.DemandMultidaysData;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
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

    private final ParseXmlRepository repository;


    public ParseXmlService(ParseXmlRepository repository) {
        this.repository = repository;
    }


    public DemandMultidaysData parseXmlToObj(String xmlString) throws Exception {
        JAXBContext context = JAXBContext.newInstance(DemandMultidaysData.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlString);

        DemandMultidaysData dmd = (DemandMultidaysData) unmarshaller.unmarshal(reader);
        return dmd;
    }


    public List<Message<DemandData>> getMessageList(DemandMultidaysData dmd) throws IOException {
        // construct result as a Message List so that DemandData are place one by one on the message queue
        List<Message<DemandData>> messages = new ArrayList<>();
        // get necessary previous demandData from mongodb
        List<DemandData> dataFromDB = repository.findTop287ByOrderByTimestampDesc();
        LocalDateTime maximumDateTime = LocalDateTime.MIN;
        if (!dataFromDB.isEmpty()) {
            maximumDateTime = dataFromDB.get(0).getTimestamp();
        }
        // dmd stores the class representing the whole XML file
        // we only need startdate and data in the 5_minute dataset
        List<DemandData> datas = dmd.getDataSet().getDatas();
        LocalDateTime startDateTime = dmd.getStartDate();
        for (int i = 0; i < datas.size(); i++) {
                LocalDateTime currDateTime = startDateTime.plusMinutes(5L * i);
                // start of blank data holders, don't need to continue on this XML file
                if (datas.get(i).getValue() == 0.0) break;
                datas.get(i).setTimestamp(currDateTime);
                messages.add(MessageBuilder.withPayload(datas.get(i)).build());
        }
        return messages;
    }
}
