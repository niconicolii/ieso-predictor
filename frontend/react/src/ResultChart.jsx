import { Line } from 'react-chartjs-2';
import 'chart.js/auto';

export function ResultChart({ dataSet }) {
    const timestamps = dataSet.map(data => data.timestamp);
    const demandValue = dataSet.map(data => data.value);

    const options = {
        scales: {
          x: {
            title: {
              display: true,
              text: 'Time',
              font: {size: 16}
            },
            ticks: {
                font: {size: 14}
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
            tension: 0.1
          }
        ]
      }
    

    return <Line data={data} options={options} />

}