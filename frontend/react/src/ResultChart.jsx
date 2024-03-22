import { Line } from 'react-chartjs-2';
import 'chart.js/auto';
import { useState } from 'react';

export function ResultChart({ dataSet, granularity }) {
  console.log(dataSet);
  const timestamps = dataSet.map(data => data.id);
  const demandValue = dataSet.map(data => data.demandValue);

  let prevDate = null;

  const options = {
      scales: {
        x: {
          title: {
            display: true,
            text: 'Time',
            font: {size: 16}
          },
          ticks: {
              font: {size: 14},
              autoSkip: false,
              callback: function(val, index, ticks) {
                let lab = this.getLabelForValue(val);
                // return as is if it's Daily hour, but need to dismiss if too many ticks
                if (granularity === 'daily') {
                  if (ticks.length <= 12 || index % Math.ceil(ticks.length / 12) === 0) {
                    return lab; 
                  }
                  return '';
                };
                const splitted = lab.split(" - ");
                // return date if 00:00
                if (splitted[1] === '00:00') {return splitted[0]};

                const mult = granularity === 'fiveMin' ? 12 : 1;
                // one day < 24(*12) => show every two hours
                // two days < 24(*12)*2 => show every 4 hours
                // three days < 24(*12)*3 => show every 6 hours
                // ==> tickes.length / 24(*12) ceil= x => show time if hour % x === 0 until x < 5?
                const divider = Math.ceil(ticks.length / (24 * mult)) * 2;
                if (divider < 10) {
                  // divider * mult since 12 data per hour in fiveMin data OR 1 data per hour in hourly data
                  if (index % (divider * mult) === 0) {
                    return splitted[1];
                  }
                }
                return '';
              }
          }
        },
        y: {
          title: {
            display: true,
            text: 'Value',
            font: {size: 16}
          },
          ticks: {
              font: {size: 14}
          }
        }
      }
  }

  const data = {
      labels: timestamps, // time
      datasets: [
        {
          label: 'Demand',
          data: demandValue,
          fill: true,
          backgroundColor: 'rgba(75, 192, 192, 0.5)',
          borderColor: 'rgb(75, 192, 192)',
          tension: 0.5
        }
      ]
    }



  return (
    <div>
      {dataSet.length == 0 && <p className='no-data-error'>No Data</p>}
      <Line data={data} options={options} />
    </div>
  )
}