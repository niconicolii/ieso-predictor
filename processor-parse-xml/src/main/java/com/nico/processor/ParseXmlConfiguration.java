package com.nico.processor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ParseXmlConfiguration {
    @Bean
    public Function<String, List<Message<String>>> parseXml() {
        return xmlData -> {
            List<Message<String>> demandDataList = new ArrayList<>();
//            example of how to do batch producer (which is sending multiple messages to exchange)
//            demandDataList.add(MessageBuilder.withPayload("a").build());
//            demandDataList.add(MessageBuilder.withPayload("b").build());
//            demandDataList.add(MessageBuilder.withPayload("c").build());
            return demandDataList;
        };
    }
}
