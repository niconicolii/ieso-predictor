package com.nico.organizeHistoricalData;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvBeanIntrospectionException;
import com.opencsv.exceptions.CsvChainedException;
import com.opencsv.exceptions.CsvFieldAssignmentException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.util.*;

public class WeatherMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    public WeatherMappingStrategy(Class<T> type) {
        setType(type);
    }

    @Override
    public T populateNewBean(String[] line)
            throws CsvBeanIntrospectionException, CsvFieldAssignmentException,
            CsvChainedException {
//        System.out.println("??????????????????? WUT?????????????????? " + Arrays.toString(line));
//        System.out.println("Line.length: " + line.length);
//        System.out.println("Header index:" + Arrays.toString(headerIndex.getHeaderIndex()));

        if (headerIndex == null || line == null) {
            throw new CsvRequiredFieldEmptyException("Missing Header");
        }
        // check length of line
        // case 1: same length as headers => continue
        // case 2: only 9 in length = no data after date and time => add a default temp and concatenate a dummy list
        // case 3: more than 9 in length  but still missing => change 10th number and concatenate missing # dummy list
        String[] headers = headerIndex.getHeaderIndex();
        List<String> newLine = new ArrayList<>(Arrays.asList(line));
        if (newLine.size() == headers.length) {
            // pass
        } else if (line.length == 9) {
            newLine.add("-1000.0");
            newLine.addAll(Collections.nCopies(headers.length - 10, null));
        } else {
            newLine.set(9, "-1000.0");
            newLine.addAll(Collections.nCopies(headers.length - line.length, null));
        }

        Map<Class<?>, Object> beanTree = createBean();
        CsvChainedException chainedException = null;

        for (int col = 0; col < newLine.size(); col++) {
            try {
                setFieldValue(beanTree, newLine.get(col), col);
            } catch (CsvFieldAssignmentException e) {
                if (chainedException != null) {
                    chainedException.add(e);
                } else {
                    chainedException = new CsvChainedException(e);
                }
            }
        }
        if (chainedException != null) {
            if (chainedException.hasOnlyOneException()) {
                throw chainedException.getFirstException();
            }
            throw chainedException;
        }
        return (T) beanTree.get(type);
    }
}
