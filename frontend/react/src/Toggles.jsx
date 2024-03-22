import { useState } from 'react';
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
            <div className='datepickers-container'>
                <div className='datepicker-wrapper'>
                    <label htmlFor='startDate'>Start Date: </label>
                    <DatePicker id='startDate' selected={startDate} onChange={date => handleNewStartDate(date)} />
                </div>
                <div className='datepicker-wrapper'>
                    <label htmlFor='endDate'>End Date: </label>
                    <DatePicker id='endDate' selected={endDate} onChange={date => handleNewEndDate(date)} />
                </div>
                <label>Data frequench: </label>
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
            {dateErrorMessage.length > 0 && <div className='toggle-error'>{dateErrorMessage}</div>}
        </div>
    )

}