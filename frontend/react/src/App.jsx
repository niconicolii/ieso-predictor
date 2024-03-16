import { ResultChart } from "./ResultChart";
import "./styles.css"
import React from 'react';


export default function App() {
    const dataSet = [
        { time: '00:00', value: 22 },
        { time: '00:05', value: 20 },
        { time: '00:15', value: 20 },
        { time: '00:20', value: 27 },
        { time: '00:25', value: 19 },
        { time: '00:30', value: 22 },
        { time: '00:35', value: 26 },
        { time: '00:40', value: 30 },
        { time: '00:45', value: 31 },
        { time: '00:50', value: 22 },
        { time: '00:55', value: 16 },
        { time: '01:00', value: 20 },
        { time: '01:05', value: 20 },
        { time: '01:10', value: 25 },
        { time: '01:15', value: 24 },
        { time: '01:20', value: 20 },
        { time: '01:25', value: 29 },
        { time: '01:30', value: 23 },
        { time: '01:35', value: 22 },
        { time: '01:40', value: 29 },
        { time: '01:45', value: 21 },
        { time: '01:50', value: 16 },
        { time: '01:55', value: 20 },
        { time: '02:00', value: 28 },
      ];


    return (
        <div className="demand-chart-container">
            <ResultChart dataSet={dataSet} />
        </div>
    )
}