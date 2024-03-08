package com.nico.processor.dataClasses;

import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.List;

public class DemandDataSet {
    private String series;
    private List<DemandData> datas;

    @XmlAttribute(name = "Series")
    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    @XmlElement(name = "Data")
    public List<DemandData> getDatas() {
        return datas;
    }

    public void setDatas(List<DemandData> datas) {
        this.datas = datas;
    }
}
