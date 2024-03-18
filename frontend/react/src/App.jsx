import { ResultChart } from "./ResultChart";
import "./styles.css"
import React, { useEffect, useState } from 'react';
import api from './api/axiosConfig';


export default function App() {
    const [demands, setDemands] = useState();
    const [isLoading, setIsLoading] = useState(true);

    const getDemands = async () => {
        try {
            const response = await api.get("/api/v1/data");
            console.log(demands);
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
        // Set up the interval to call getDemands every 5 minutes
        const interval = setInterval(getDemands, 300000);
        // Clear the interval
        return () => clearInterval(interval);
    }, []);



    if (isLoading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="demand-chart-container">
            <ResultChart dataSet={demands} />
        </div>
    )
}