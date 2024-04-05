package com.nico.organizeHistoricalData.services;

import com.nico.organizeHistoricalData.repository.EnergyCSVRow;
import com.nico.organizeHistoricalData.repository.WEathergyData;
import com.nico.organizeHistoricalData.repository.WEathergyRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class EnergyDataService {
    private Path energySaveDir;
    private String urlBase;
    private final WEathergyRepository repository;

    @Autowired
    public EnergyDataService(WEathergyRepository repository) throws IOException {
        this.repository = repository;
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        this.energySaveDir = rootPath.resolve("data/energy");
        Files.createDirectories(this.energySaveDir);
        this.urlBase = "http://reports.ieso.ca/public/Demand/";
    }

    public String downloadEnergyCsv(int year) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String fileName = "PUB_Demand_" + year + ".csv";
        byte[] fileBytes = restTemplate.getForObject(urlBase + fileName, byte[].class);
        if (fileBytes != null) {
            Files.write(energySaveDir.resolve(fileName), fileBytes);
            System.out.println("Downloaded " + fileName);
            return fileName;
        }
        throw new IOException(String.format("Failed to download %d's energy demand csv file.", year));}

    public List<EnergyCSVRow> csvToRowList(String fileName) throws FileNotFoundException, IOException {
        try (FileReader fileReader = new FileReader(energySaveDir.resolve(fileName).toString())) {
            return new CsvToBeanBuilder<EnergyCSVRow>(fileReader)
                    .withType(EnergyCSVRow.class)
                    .withSkipLines(3)
                    .build()
                    .parse();
        }
    }

    public void addEnergyDemandToDB(int year) throws IOException {
        String fileName = downloadEnergyCsv(year);
        List<EnergyCSVRow> energyCSVRowList = csvToRowList(fileName);
        for (EnergyCSVRow row : energyCSVRowList) {
            long dt = row.getDt();
            repository.findWEathergyDataByDt(dt).ifPresentOrElse(
                    wEathergyData -> {
                        if (wEathergyData.getDemand() == row.getDemand()) {
//                            System.out.println("No need to update on exactly same demand.");
                        } else {
                            wEathergyData.setDemand(row.getDemand());
                            repository.save(wEathergyData);
                            System.out.println("Updated WEathergyData: " + wEathergyData.toString());
                        }
                    },
                    () -> {
                        WEathergyData data = new WEathergyData(dt, row.getDemand());
                        repository.insert(data);
                        System.out.println("Created and inserted new WEathergyData" + data.toString());
                    }
            );
        }
    }
}
