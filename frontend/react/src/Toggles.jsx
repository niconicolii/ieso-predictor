import { useEffect, useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

export function Toggles({ updateGranularity, updateStartDate, updateEndDate }) {
    const [startDate, setStartDate] = useState(new Date());
    const [endDate, setEndDate] = useState(new Date());
    const [dateErrorMessage, setDateErrorMessage] = useState('');
    
    const [selectedStats, setSelectedStats] = useState({
        fiveMin: true,
        hourly: false,
        daily: false,
    });

    function handleNewStartDate(date) {
        if (date > endDate) {
            setDateErrorMessage('Start Date cannot be later than End Date!');
            return
        }
        setDateErrorMessage('');
        updateStartDate(date);
        setStartDate(date);
    }

    function handleNewEndDate(date) {
        if (date < startDate) {
            setDateErrorMessage('End Date cannot be earlier than Start Date!');
            return
        }
        setDateErrorMessage('');
        updateEndDate(date);
        setEndDate(date);
    }


    function handleNewSelection(granularity) {
        setSelectedStats({
            fiveMin: false,
            hourly: false,
            daily: false,
            [granularity]: true,
        })
        updateGranularity(granularity);
    }

    return (
        <div>
            <div className='lonely-line'>
                Start Date: <DatePicker className='graphDatePicker' selected={startDate} onChange={date => handleNewStartDate(date)} />
                End Date: <DatePicker className='graphDatePicker' selected={endDate} onChange={date => handleNewEndDate(date)} />
                <label className='toggles-inputs'>
                    <input type="checkbox" checked={selectedStats.fiveMin} onChange={e => {handleNewSelection('fiveMin')}} />
                    5 Minute
                </label>
                <label className='toggles-inputs'>
                    <input type="checkbox" checked={selectedStats.hourly} onChange={e => {handleNewSelection('hourly')}} />
                    Hourly
                </label>
                <label className='toggles-inputs'>
                    <input type="checkbox" checked={selectedStats.daily} onChange={e => {handleNewSelection('daily')}} />
                    Daily
                </label>
            </div>
            <div className='toggle-error'>{dateErrorMessage}</div>
        </div>
    )

}