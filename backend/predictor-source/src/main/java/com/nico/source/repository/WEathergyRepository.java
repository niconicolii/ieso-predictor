package com.nico.source.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WEathergyRepository extends MongoRepository<WEathergyData, String> {

    Optional<WEathergyData> findWEathergyDataByDt(long dt);

    List<WEathergyData> findAllByOrderByDtAsc();
}
