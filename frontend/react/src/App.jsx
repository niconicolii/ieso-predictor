import { ResultChart } from "./ResultChart";
import "./styles.css"
import React, { useEffect, useState } from 'react';
import { format } from 'date-fns';
import api from './api/axiosConfig';
import { Toggles } from "./Toggles";


export default function App() {
    const dateFormat = 'yyyy-MM-dd';
    const [demands, setDemands] = useState([]);
    const [isLoading, setIsLoading] = useState(true);   // TODO: consider removing
    const [restCoolDown, setRestCoolDown] = useState(10);
    const [startDate, setStartDate] = useState(new Date());
    const [endDate, setEndDate] = useState(new Date());
    const [dataGranularity, setDataGranularity] = useState('fiveMin');

    const getDemands = async () => {
        try {
            const url = `/${dataGranularity}?start=${format(startDate, dateFormat)}&end=${format(endDate, dateFormat)}`
            const response = await api.get(url);
            setDemands(() => {
                return [...response.data];
            });
        } catch(err) {
            console.log(err);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        getDemands();
    }, [dataGranularity, startDate, endDate]);



    useEffect(() => {
        setIsLoading(true);
        getDemands();
        const eventSource = new EventSource("http://localhost:8080/updates");
        let debounceTimer;
        eventSource.onmessage = (event) => {
            // console.log(event);
            // clearTimeout if a new event enters within 1 second the previous event setted the Timer
            clearTimeout(debounceTimer);

            debounceTimer = setTimeout(() => {
                // getDemand after 1 sec of silence!
                getDemands();
            }, 1000);
        };
        
        eventSource.onerror = (error) => {
            console.error('EventSource failed:', error);
            eventSource.close();
        };
        
        return () => eventSource.close();
    }, [])



    if (isLoading) {
        return <div>Loading...</div>;
    }

    function updateStartDate(date) {
        setStartDate(date);
    }

    function updateEndDate(date) {
        setEndDate(date);
    }

    const handleGranularityChange = (granularity) => {
        setDataGranularity(granularity);
    }

    

    return (
        <>
            <div className="toggles-container">
                <Toggles updateGranularity={handleGranularityChange} updateStartDate={updateStartDate} updateEndDate={updateEndDate} />
            </div>
            <div className="demand-chart-container">
                <ResultChart dataSet={demands} granularity={dataGranularity} />
            </div>
        </>
    )
}