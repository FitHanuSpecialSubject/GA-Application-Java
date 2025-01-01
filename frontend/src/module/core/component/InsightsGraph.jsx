import { Line } from "react-chartjs-2";

const colors = ["#262A56", "#45CFDD", "#6527BE", "#E11299", "#EBB02D", "hotpink", "crimson", "bluesteel", "lime"];

export default function InsightsGraph({ runtimes }) {
  const algorithms = Object.keys(runtimes);
    const labels = runtimes[algorithms[0]].map((_, index) => index + 1);
    const datasets = algorithms.map((name, index) => {
        return {
            label: name,
            data: runtimes[name],
            fill: false,
            // wrap around if index too big
            borderColor: colors[index % colors.length],
            tension: 0.1
        };
    });
  const graphData = {
      labels: labels,
    datasets: datasets,
  };

  const graphOptions = {
    responsive: true,
    maintainAspectRatio: false,
    width: 600,
    height: 600,
  };

  return <Line class="graph" data={graphData} option={graphOptions} />;
}
