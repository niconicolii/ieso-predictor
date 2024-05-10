package com.nico.processor.service;

import com.nico.processor.csv.EnergyCSVRow;
import com.nico.processor.dataClasses.WEathergyData;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class EnergyDataService {
    private final Path energySaveDir;
    private final String urlBase;
    private final HelperService helperService;
//    private final WEathergyRepository repository;

    @Autowired
    public EnergyDataService(HelperService helperService) throws IOException {
        this.helperService = helperService;
//        this.repository = repository;
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        this.energySaveDir = rootPath.resolve("data/energy");
        Files.createDirectories(this.energySaveDir);
        this.urlBase = "http://reports.ieso.ca/public/Demand/";
    }

    public String downloadEnergyCsv(int year) throws IOException {
        // check if file already existing, if exist also check if it's updated (downloaded after 8:00AM today)
        String fileName = "PUB_Demand_" + year + ".csv";
        Path filePath = energySaveDir.resolve(fileName);
        File file = new File(filePath.toString());
        long lastModified = file.lastModified() / 1000;
        if (lastModified < helperService.getTodayAtHourDt(8)) {
            // then we need to download the file
            RestTemplate restTemplate = new RestTemplate();
            byte[] fileBytes = restTemplate.getForObject(urlBase + fileName, byte[].class);
            if (fileBytes != null) {
                Files.write(energySaveDir.resolve(fileName), fileBytes);
                System.out.println("Downloaded " + filePath);
            } else throw new IOException(String.format("Failed to download %d's energy demand csv file.", year));
        }
        // return file name so later process could read csv file
        return fileName;
    }

    public List<EnergyCSVRow> csvToRowList(String fileName) throws FileNotFoundException, IOException {
        try (FileReader fileReader = new FileReader(energySaveDir.resolve(fileName).toString())) {
            return new CsvToBeanBuilder<EnergyCSVRow>(fileReader)
                    .withType(EnergyCSVRow.class)
                    .withSkipLines(3)
                    .build()
                    .parse();
        }
    }

    public List<WEathergyData> fillDemandIntoWEathergy(List<WEathergyData> wEathergies) throws IOException {
        int year = helperService.dtToYear(wEathergies.get(0).getDt());
        long startOfYear = helperService.localYmdtToDT(year, 1, 1, 1);
        String fileName = downloadEnergyCsv(year);
        List<EnergyCSVRow> energyCSVRows = csvToRowList(fileName);
        int index = 0;
        for (EnergyCSVRow row : energyCSVRows) {
            long csvDt = row.getDt();
            if (csvDt == wEathergies.get(index).getDt()) {
                wEathergies.get(index).setDemand(row.getDemand());
                System.out.println(wEathergies.get(index).toString());
                index ++;
                if (index >= wEathergies.size()) break;
            }
        }
        return wEathergies;
    }

}
