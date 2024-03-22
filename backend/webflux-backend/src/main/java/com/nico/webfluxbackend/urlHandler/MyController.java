package com.nico.webfluxbackend.urlHandler;

import com.nico.webfluxbackend.database.DemandData;
import com.nico.webfluxbackend.database.DemandDataRepository;
import com.nico.webfluxbackend.database.DemandDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
public class MyController {
    @Autowired
    private DemandDataRepository repository;

    @GetMapping("/data")
    public Flux<DemandData> getFiveMin(){
        return repository.findAll()         // 从数据库要所有数据 => 数据库它还给我们的是一个Flux=Publisher => 它把数据一个一个发布给我们
                                            // => 我们的getFiveMin就会变成 数据库给一个data我们就发给前端一个data
                .delayElements(Duration.ofMillis(1000));
    }

}
