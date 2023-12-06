let stockData = [];
let stockDealCount = [];
let stockPerChange = [];
let stockMeanPrice = [];
let label = generateTimeLabels();
function generateTimeLabels() {
  const labels = [];
  const morningStartTime = new Date("2023-01-01T09:30:00");
  const morningEndTime = new Date("2023-01-01T11:30:00");
  const afternoonStartTime = new Date("2023-01-01T13:00:00");
  const afternoonEndTime = new Date("2023-01-01T15:00:00");

  let currentTime = new Date(morningStartTime);

  while (currentTime <= morningEndTime) {
    labels.push(currentTime.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit' }));
    currentTime.setMinutes(currentTime.getMinutes() + 1);
  }

  currentTime = new Date(afternoonStartTime);

  while (currentTime <= afternoonEndTime) {
    labels.push(currentTime.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit' }));
    currentTime.setMinutes(currentTime.getMinutes() + 1);
  }

  return labels;
}

const ctx = document.getElementById('stockChart').getContext('2d');
const config = {
  type: 'line',
  data: {
    labels: label,
    datasets: [{
      label: '价格',
      borderColor: 'blue',
      borderWidth: 2,
      data: stockData,
      fill: false,
    },
      {
        label: '成交量',
        data: stockDealCount,
        hidden: true,
      },
      {
        label: '涨跌率',
        data: stockPerChange,
        hidden: true,
      },
      {
        label: '均价',
        data: stockMeanPrice,
        hidden: false,
      }],
  },
  options: {
    responsive: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: function (context) {
            if (context.datasetIndex == 0) {
              let label = context.dataset.label || '';
              let curPrice = context.parsed.y.toFixed(2);
              let dealCount = stockChart.data.datasets[1].data[context.dataIndex];
              let perChange = (stockChart.data.datasets[2].data[context.dataIndex]*100).toFixed(2);
              let labels = [];
              labels.push(label + ': ' + curPrice);
              labels.push('成交量: ' + dealCount);
              labels.push('涨跌率: ' + perChange + "%");
              return labels;
            }
          }
        }
      },
      title: {
        display: true,
        text: '股票分时图'
      }
    },
    hover: {
      mode: 'index',
      intersect: false
    },
    scales: {
      x: {
        title: {
          display: true,
          text: 'Time'
        },
        grid: {
          display: false,
        },
        ticks:{
          autoSkip: false,
          callback: function(val, index) {
            return index % 30 === 0 ? this.getLabelForValue(val) : '';
          },

        },
      },
      y: {
        title: {
          display: true,
          text: 'Price'
        },
        suggestedMin: 3000,
        suggestedMax: 3050,
        // ticks: {
        //   // forces step size to be 50 units
        //   stepSize: 2
        // }
      }
    },
    elements: {
      point: {
        radius: 1, // 设置数据点的大小
      },
    },
  },
};

const stockChart = new Chart(ctx, config);

let stompClient = null;
const socket = new SockJS('/websocket');
stompClient = Stomp.over(socket);

let stockCodeUpdate = null;
let stockCodeUpdatePerMin = null;
let startName = null;
let startData = null;

stompClient.connect({}, function (frame) {
  console.log('Connected: ' + frame);
  subscribeStockData('sh000001');
});

function checkStockCodeValid(stockCode) {
  if (stockCode === null || stockCode === undefined ||stockCode.length === 0) {
    console.log("请输入股票号码");
    document.getElementById("stockCodeInput").placeholder = "请输入股票号码";
    return false;
  }
  if (stockCode.length !== 8) {
    document.getElementById("stockCodeInput").value = "";
    document.getElementById("stockCodeInput").placeholder = "请输入正确股票号码";
    return false;
  }
  return true;
}

function updateText(data) {
  document.getElementById("price").innerHTML = data.curPrice.toFixed(2) +"  ";
  document.getElementById("price_large").innerHTML = data.curPrice.toFixed(2);
  const change = parseFloat(data.curPrice) - stockData[0];
  const perChange = change / stockData[0] *100;
  document.getElementById("grow").innerHTML = perChange.toFixed(2) +"%  ";
  document.getElementById("perGrow").innerHTML = perChange.toFixed(2) +"%";
  document.getElementById("absGrow").innerHTML = change.toFixed(2)+"";

  document.getElementById("dealCount").innerHTML = data.dealCount +"  ";
  document.getElementById("time").innerHTML = data.timeStampChina;
  document.getElementById("cost").innerHTML = (new Date() - new Date(data.fetchTime)) + "ms";
  if (data.curPrice > stockData[0]) {
    document.getElementById("text_content").style.color = "red";
    document.getElementById("text_content").querySelector('#stockName').style.color = "red";
  } else {
    document.getElementById("text_content").style.color = "#2ecc71";
    document.getElementById("text_content").querySelector('#stockName').style.color = "#2ecc71";
  }
}
function subscribeStockData(stockCode) {

  // 发送股票号到后端
  stompClient.send('/app/stock', {}, stockCode);

  // 取消之前订阅显示
  if(startName) {
    startName.unsubscribe();
  }
  if(startData) {
    startData.unsubscribe();
  }
  if(stockCodeUpdate) {
    stockCodeUpdate.unsubscribe();
  }
  if(stockCodeUpdatePerMin) {
    stockCodeUpdatePerMin.unsubscribe();
  }

  startName = stompClient.subscribe('/topic/initialStockName/'+stockCode, function (message) {
    const data = message.body;

    document.getElementById("stockName").innerHTML = data;
    // if (data === "Stock Not Found, 检查输入股票号码") {
    //   return;
    // }
    // document.getElementById("unsubscribe").innerHTML = "取消订阅"
    console.log(data);
    console.log("should get name");
  });

  // 订阅股票数据
  startData = stompClient.subscribe('/topic/initialStockData/'+stockCode, function (message) {
    const data = JSON.parse(message.body);
    // 在这里处理从后端获取的股票数据
    // labels = stockData.map(data => data.timeStampChina.slice(11, -3));
    stockData = data.map(data => data.curPrice);
    stockDealCount = data.map(data => data.dealCount);
    stockPerChange = data.map(data => data.perChange);
    stockMeanPrice = data.map(data => data.meanPrice);
    stockChart.data.datasets[0].data = stockData;
    stockChart.data.datasets[1].data = stockDealCount;
    stockChart.data.datasets[2].data = stockPerChange;
    stockChart.data.datasets[3].data = stockMeanPrice;
    const chartConfig = stockChart.config;
    chartConfig.options.scales.y.suggestedMin = stockData[0] * 0.99;
    chartConfig.options.scales.y.suggestedMax = stockData[0] *  1.01;
    console.log(chartConfig);
    stockChart.update();
    console.log(stockData.length);
    console.log("should initially update");
    updateText(data[data.length-1]);

  });

  stockCodeUpdate = stompClient.subscribe('/topic/updatedStockData/'+stockCode, function (message) {
    const newdata = JSON.parse(message.body);
    console.log(newdata);
    updateText(newdata);
    // document.getElementById("price").innerHTML = newdata.curPrice +"  ";
    // document.getElementById("price_large").innerHTML = newdata.curPrice;
    // document.getElementById("grow").innerHTML = newdata.perChange +"%  ";
    // document.getElementById("absGrow").innerHTML = (parseFloat(newdata.curPrice) - stockData[0]).toFixed(2)+"";
    // document.getElementById("perGrow").innerHTML = newdata.perChange +"%";
    // document.getElementById("dealCount").innerHTML = newdata.dealCount +"  ";
    // document.getElementById("time").innerHTML = newdata.timeStampChina;
    // document.getElementById("cost").innerHTML = (new Date() - new Date(newdata.fetchTime)) + "ms";
    // if (newdata.curPrice > stockData[0]) {
    //   document.getElementById("text_content").style.color = "red";
    //   document.getElementById("text_content").querySelector('#stockName').style.color = "red";
    // } else {
    //   document.getElementById("text_content").style.color = "#2ecc71";
    //   document.getElementById("text_content").querySelector('#stockName').style.color = "#2ecc71";
    // }
  });

  stockCodeUpdatePerMin = stompClient.subscribe('/topic/updatedStockDataPerMin/'+stockCode, function (message) {
    const newdata = JSON.parse(message.body);
    console.log("Full Min" + newdata.curPrice + " / +" + newdata.dealCount + " / +" + newdata.perChange);
    stockData.push(newdata.curPrice);
    stockDealCount.push(newdata.dealCount);
    stockPerChange.push(newdata.perChange);
    stockMeanPrice.push(newdata.meanPrice);
    stockChart.data.datasets[0].data = stockData;
    stockChart.data.datasets[1].data = stockDealCount;
    stockChart.data.datasets[2].data = stockPerChange;
    stockChart.data.datasets[3].data = stockMeanPrice;
    stockChart.update();
    console.log(stockData.length);
  });
}

// 提交表单时获取输入的股票号
document.getElementById('stockForm').addEventListener('submit', function (event) {
  event.preventDefault();
  const stockCodeInput = document.getElementById('stockCodeInput').value;
  const valid = checkStockCodeValid(stockCodeInput);
  if (valid) {
    subscribeStockData(stockCodeInput);
  }
});

// function unsubscribeStockData(stockCode) {
//   stompClient.send('/app/stock/unsubscribe', {}, stockCode);
//   stompClient.subscribe('/topic/unsubscribe', function (message) {
//     const data = message.body;
//     // document.getElementById("unsubscribe").innerHTML = "已取消"
//     console.log("Unsubscribed");
//   });
// }
// document.getElementById('stockForm').addEventListener('unsubscribe', function (event) {
//   event.preventDefault();
//   const stockCodeInput = document.getElementById('stockCodeInput').value;
//   unsubscribeStockData(stockCodeInput)
// });



// 获取当前时间
function getCurrentTime() {
  const currentTime = new Date();
  let month = currentTime.getMonth() + 1;
  let day = currentTime.getDate();
  let hours = currentTime.getHours();
  let minutes = currentTime.getMinutes();
  let seconds = currentTime.getSeconds();

  // 格式化时间
  month = (month < 10) ? "0" + month : month;
  day = (day < 10) ? "0" + day : day;
  hours = (hours < 10) ? "0" + hours : hours;
  minutes = (minutes < 10) ? "0" + minutes : minutes;
  seconds = (seconds < 10) ? "0" + seconds : seconds;

  // 将时间显示在页面上
  const timeString = month + "/" + day + " " + hours + ":" + minutes + ":" + seconds;
  document.getElementById("timeNow").innerHTML = timeString;
}

// 初始加载时获取并显示时间
getCurrentTime();

// 每秒更新一次时间
setInterval(getCurrentTime, 1000);