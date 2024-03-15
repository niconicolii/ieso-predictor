package com.nico.processor.dataClasses;


import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.List;

@XmlRootElement(name = "Root")
public class DemandMultidaysData{

    private String startDateStr;
    private LocalDateTime startDate;
    private List<DemandDataSet> dataSets;
    private DemandDataSet dataSet;

    @XmlElement(name = "StartDate")
    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }


    @XmlElement(name = "DataSet")
    public List<DemandDataSet> getDataSets() {
        return dataSets;
    }

    public LocalDateTime getStartDate() {
        if (startDate == null) {
            startDate = LocalDateTime.parse(getStartDateStr());
        }
        return startDate;
    }

    public void setDataSets(List<DemandDataSet> dataSets) {
        this.dataSets = dataSets;
    }

    public DemandDataSet getDataSet() {
        if (dataSet == null) {
            for (DemandDataSet temp : getDataSets()) {
                if (temp.getSeries().equals("5_Minute")) {
                    dataSet = temp;
                    return dataSet;
                }
            }
        }
        return dataSet;
    }

    public void printDataSet() {
        System.out.println("===============================================\n" +
                "5 Minutes Demand values started at " + getStartDate() + " : ");
        for (DemandData curr : getDataSet().getDatas()) {
            System.out.println("Value: " + curr.getValue());
        }
    }

    public void assignTimeStamps() {
        List<DemandData> datas = getDataSet().getDatas();
        for (int count = 0; count < datas.size(); count++) {
            datas.get(count).setTimestamp(getStartDate().plusMinutes(5L * count));
//            System.out.println("Timestamp: " + datas.get(count).getTimestamp() +
//                    "; Value: " + datas.get(count).getValue());
        }
    }
}
