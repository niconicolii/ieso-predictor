package com.nico.processor;

import com.nico.processor.dataClasses.DemandData;
import com.nico.processor.dataClasses.DemandMultidaysData;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.cglib.core.Local;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public LocalDateTime getMaxDateTime() {
        Optional<DemandData> optional = repository.findTopByOrderByTimestampDesc();
        if (optional.isPresent()) {
            return optional.get().getTimestamp();
        }
        return LocalDateTime.MIN;
    }

    public Message<DemandData> buildMessage(DemandData data) {
        return MessageBuilder.withPayload(data).build();
    }

    public List<Message<DemandData>> getMessageList(DemandMultidaysData dmd) throws IOException {
        // construct result as a Message List so that DemandData are place one by one on the message queue
        List<Message<DemandData>> messages = new ArrayList<>();

        LocalDateTime maximumDateTime = getMaxDateTime();

        // dmd stores the class representing the whole XML file
        // we only need startdate and data in the 5_minute dataset
        List<DemandData> datas = dmd.getDataSet().getDatas();
        LocalDateTime startDateTime = dmd.getStartDate();
        for (int i = 0; i < datas.size(); i++) {
                // start of blank data holders, don't need to continue on this XML file
                if (datas.get(i).getValue() == 0.0) break;

                // only insert data if calculated datetime greater than maximum datetime
                LocalDateTime currDateTime = startDateTime.plusMinutes(5L * i);
                if (currDateTime.isAfter(maximumDateTime)) {
                    datas.get(i).setTimestamp(currDateTime);
                    messages.add(buildMessage(datas.get(i)));
                }
        }
        return messages;
    }
}
