import React from "react";
import "../../module/stableMatching/css/output.scss";
import GraphImage from "../../module/core/asset/image/graph.png";
import { useContext, useState } from "react";
import DataContext from "../../module/core/context/DataContext";
import { useNavigate } from "react-router-dom";
import NothingToShow from "../../module/core/component/NothingToShow";
import Loading from "../../module/core/component/Loading";
import Popup from "../../module/core/component/Popup";
import axios from "axios";
import ParamSettingBox from "../../module/core/component/ParamSettingBox";
import PopupContext from "../../module/core/context/PopupContext";
import * as XLSX from '@e965/xlsx';
import SockJS from "sockjs-client";
import { v4 } from "uuid";
import { over } from "stompjs";
import { saveAs } from 'file-saver';
import Table from "react-bootstrap/Table";
import Button from "react-bootstrap/Button";
import {getBackendAddress} from '../../utils/http_utils';
import { createSystemInfoSheet, createParameterConfigSheet , loadProblemDataOld, loadProblemDataParallel } from '../../utils/excel_utils.js';




let stompClient = null;
export default function MatchingOutputPage() {
  const navigate = useNavigate();
  const { appData, setAppData } = useContext(DataContext);
  const [isLoading, setIsLoading] = useState(false);
  const [isShowPopup, setIsShowPopup] = useState(false);
  const { displayPopup } = useContext(PopupContext);
  const [sessionCode, setSessionCode] = useState(v4());
  const [loadingMessage, setLoadingMessage] = useState(
    "Processing to get problem insights, please wait..."
  );
  const [loadingEstimatedTime, setLoadingEstimatedTime] = useState(null);
  const [loadingPercentage, setLoadingPercentage] = useState();
  const [distributedCoreParam, setDistributedCoreParam] = useState("all");
  const [populationSizeParam, setPopulationSizeParam] = useState(1000);
  const [generationParam, setGenerationParam] = useState(100);
  const [maxTimeParam, setMaxTimeParam] = useState(5000);
  const [selectedSet, setSelectedSet] = useState("all");
  const problemType = appData.problemType;  




  
  const navigateToHome = () => {
    setAppData(null);
    navigate("/");
  };

  if (appData == null) {
    return <NothingToShow />;
  }
  const matchesArray = appData.result.data.matches.matches;
  const leftOversArray = appData.result.data.matches.leftOvers;
  const inputIndividuals = appData.problem.individuals
  const problemData = appData.problem;
  // Handle filter change
  const handleSetFilterChange = (event) => {
    setSelectedSet(event.target.value); // Cập nhật giá trị đã chọn
  };

// Lọc dữ liệu theo giá trị selectedSet
const filteredMatches = selectedSet === "all" 
  ? matchesArray 
  : matchesArray.filter((_, index) => {
      const individual = inputIndividuals[index]; // Lấy cá nhân tại chỉ số index
      return individual?.setType === (Number(selectedSet) - 1); // So sánh với setType - 1
    });

  const scroll = (pos) => {
    document.body.scrollTop = pos; // For Safari
    document.documentElement.scrollTop = pos;
  }

  const handleExportToExcel = async () => {
    const workbook = XLSX.utils.book_new();
    // write result data to sheet 1
    const sheet1 = XLSX.utils.aoa_to_sheet([
      ["Fitness value", appData.result.data.fitnessValue],
      ["Used algorithm", appData.result.params.usedAlgorithm],
      ["Runtime (in seconds)", appData.result.data.runtime],
      ["Individual Name", "Individual Matches", "Satisfaction value"],
    ]);

    // append players data to sheet 1
    // matchesArray.forEach((match,index) => {
    //   const row = [match.individualName, match.individualMatches, match.setSatisfactions];
    //   XLSX.utils.sheet_add_aoa(sheet1, [row], { origin: -1 });
    // });
    matchesArray.forEach((match, index) => {
      let individualName = problemData.individualNames[index]

      let individualMatches = "";
      if (Object.values(match).length===0) {
        individualMatches = "There are no individual matches";
      } else {
        for (let i = 0; i < Object.values(match).length; i++) {
          let name = problemData.individualNames[Object.values(match)[i]];
          if (i === Object.values(match).length - 1) {
            individualMatches += name;
          }else
          individualMatches += name + ", ";
        }
        const row=[individualName, individualMatches, appData.result.data.setSatisfactions[index].toFixed(3)]
        console.log(row);
        XLSX.utils.sheet_add_aoa(sheet1, [row], { origin: -1 });
  }})
    // write parameter configurations to sheet 2
    const sheet2 = createParameterConfigSheet(appData);

    //TODO: tách thành một hàm riêng vào src/utils/excel_utils.js, sử dụng chung cho bên GT (nhớ test lại)
    // write computer specs to sheet 3
    const sheet3 = createSystemInfoSheet(appData);
    // append sheets to workbook
    XLSX.utils.book_append_sheet(workbook, sheet1, "Optiomal solution");
    XLSX.utils.book_append_sheet(workbook, sheet2, "Parameter Configurations");
    XLSX.utils.book_append_sheet(workbook, sheet3, "Computer Specifications");

    // write workbook to file
    const wbout = await XLSX.write(workbook, {
      bookType: "xlsx",
      type: "array",
    });
    const blob = new Blob([wbout], { type: "application/octet-stream" });
    saveAs(blob, "result.xlsx");
  };

  const handleGetMoreInsights = () => {
    setIsShowPopup(true);
  };

  const handlePopupOk = async () => {
    try {
      const evaluateFunctions = appData.problem.evaluateFunctions || [];

      setIsShowPopup(false);
      const body = {
        problemName: appData.problem.nameOfProblem,
        numberOfSets: appData.problem.numberOfSets,
        numberOfIndividuals: appData.problem.numberOfIndividuals,
        numberOfProperty: appData.problem.characteristics.length,
        individualSetIndices: appData.problem.individualSetIndices,
        individualCapacities: appData.problem.individualCapacities,
        individualProperties: appData.problem.individualProperties,
        individualRequirements: appData.problem.individualRequirements,
        individualWeights: appData.problem.individualWeights,
        fitnessFunction: appData.problem.fitnessFunction,
        evaluateFunctions: evaluateFunctions,

        distributedCores: distributedCoreParam,
        populationSize: populationSizeParam,
        generation: generationParam,
        maxTime: maxTimeParam,
      };

      let problemType = appData.problemType;
      let serviceEndpoint = problemType.insightEndpoint;
      let endpoint = `${getBackendAddress()}${serviceEndpoint}/${sessionCode}`;

      setIsLoading(true);
      await connectWebSocket(); // connect to websocket to get the progress percentage
      const res = await axios.post(endpoint, body);
      setIsLoading(false);

      const insights = {
        data: res.data.data,
        params: {
          distributedCoreParam: distributedCoreParam,
          populationSizeParam: populationSizeParam,
          generationParam: generationParam,
          maxTimeParam: maxTimeParam,
        },
      };
      setAppData({ ...appData, insights });
      closeWebSocketConnection();
      navigate("/insights"); // navigate to insights page
    } catch (err) {
      console.error(err);
      setIsLoading(false);
      displayPopup(
        "Something went wrong!",
        "Get insights failed!, please contact the admin!",
        true
      );
    }
  };

  const connectWebSocket = async () => {
    let Sock = new SockJS(
      `${getBackendAddress()}/ws`
    );
    stompClient = over(Sock);
    await stompClient.connect({}, onConnected, onError);
  };
  const onConnected = () => {
    stompClient.subscribe(
      "/session/" + sessionCode + "/progress",
      onPrivateMessage
    );
    console.log("Connected to websocket server!");
  };

  const onError = (err) => {
    console.log(err);
    // displayPopup("Something went wrong!", "Connect to server failed!, please contact the admin!", true)
  };

  const closeWebSocketConnection = () => {
    if (stompClient) {
      stompClient.disconnect();
    }
  };

  const onPrivateMessage = (payload) => {
    let payloadData = JSON.parse(payload.body);

    // some return data are to show the progress, some are not
    // if the data is to show the progress, then it will have the estimated time and percentage
    if (payloadData.inProgress) {
      setLoadingEstimatedTime(payloadData.minuteLeft);
      setLoadingPercentage(payloadData.percentage);
    }

    setLoadingMessage(payloadData.message);
  };

  //Get data from sever


  console.log(appData.result.data);
  const fitnessValue = appData.result.data.fitnessValue.toFixed(3);
  const usedAlgorithm = appData.result.data.algorithm;
  const runtime = appData.result.data.runtime.toFixed(3);
  const htmlOutput = [];
  const htmlLeftOvers = [];

  console.log(appData);

  // Loop through result

  let fileContent = "";
  
  // Success couple
  matchesArray.forEach((match, index) => {
    // Lấy individualSet từ individualSetIndices (mặc định là 0 nếu không có)
    let individualSet = appData.problem.individualSetIndices?.[index] ?? 0;
  
    // Kiểm tra nếu set đã chọn không phải là "all" và không khớp với individualSet
    if (selectedSet !== "all" && individualSet !== (Number(selectedSet) - 1)) {
      return; // Bỏ qua phần tử không thuộc set đã chọn
    }
  
    // Lấy tên cá nhân
    let individualName = problemData.individualNames?.[index] || "Unknown";
  
    let individualMatches = "";
  
    // Kiểm tra nếu match tồn tại và có giá trị
    if (!match || Object.keys(match).length === 0) {
      individualMatches = "There are no individual matches";
    } else {
      Object.values(match).forEach((matchIndex, i, arr) => {
        // Lấy tên cá nhân từ individualNames
        let name = problemData.individualNames?.[matchIndex] || "Unknown";
        individualMatches += name + (i === arr.length - 1 ? "" : ", ");
      });
    }
  
    // Thêm thông tin vào fileContent
    fileContent += `${individualName} -> ${individualMatches}\n`;
  
    // Đẩy dữ liệu vào htmlOutput
    htmlOutput.push(
      <tr className="table-success" key={`C${index + 1}`}>
        <td>{individualName}</td>
        <td>{individualMatches}</td>
        <td>{appData.result?.data?.setSatisfactions?.[index]?.toFixed(3) || 0}</td>
        <td>Set {individualSet + 1}</td> {/* Hiển thị set đúng như "Set 1" */}
      </tr>
    );
  });
  
  




  
  
  

  
  
  
  

  // LeftOves
  let leftoverArray = [];
  leftOversArray.forEach((individual, index) => {
    htmlLeftOvers.push(
      <tr className="table-danger" key={"L" + index}>
        <td>{index+1}</td>
        <td>{problemData.individualNames[individual]}</td>
        <td >Set {problemData.individualSetIndices[individual] + 1}</td> {/* Hiển thị set */}
      </tr>
    );
    leftoverArray.push(problemData.individualNames[individual]);
  });
  fileContent += `Left over = [${leftoverArray}]`;

  fileContent += `Fitness value: ${appData.result.data.fitnessValue}`;
  fileContent += `Runtime: ${appData.result.data.runtime}`;
  // Create a Blob with the content
  const blob = new Blob([fileContent], { type: "text/plain" });

  // Create a download link
  const downloadLink = document.createElement("a");
  downloadLink.href = URL.createObjectURL(blob);
  downloadLink.download = "output.txt";

    // Define your state variables here
  return (
      <div className="matching-output-page">
        <div className="scrollPanel">
          <button className="autoscrollButton" onClick={() => scroll(0)}>
            &#11165;
          </button>
          <button className="autoscrollButton" onClick={() => scroll(document.body.scrollHeight)}>
            &#11167;
          </button>
        </div>
        <h2 id="head-title">MATCHING THEORY OUTPUT PAGE</h2>
        <Popup
            isShow={isShowPopup}
            setIsShow={setIsShowPopup}
            title={"Get detailed insights"}
            // message={`This process can take estimated ${data.estimatedWaitingTime || 1} minute(s) and you will be redirected to another page. Do you want to continue?`}
            message={`This process can take a while do you to continue?`}
            okCallback={handlePopupOk}
        />

        {/* <Loading isLoading={isLoading} message={`Get more detailed insights. This can take estimated ${data.estimatedWaitingTime || 1} minute(s)...`} /> */}
        <Loading
            isLoading={isLoading}
            percentage={loadingPercentage}
            estimatedTime={loadingEstimatedTime}
            message={loadingMessage}
        />
        <br/>
        <p className="below-headertext">Optimal solution</p>
        <div className="output-container">
          <div className="param-box">
            <ParamSettingBox
                distributedCoreParam={distributedCoreParam}
                setDistributedCoreParam={setDistributedCoreParam}
                generationParam={generationParam}
                setGenerationParam={setGenerationParam}
                populationSizeParam={populationSizeParam}
                setPopulationSizeParam={setPopulationSizeParam}
                maxTimeParam={maxTimeParam}
                setMaxTimeParam={setMaxTimeParam}
            />
            <div className="btn insight-btn" onClick={handleGetMoreInsights}>
              <img src={GraphImage} alt=""/>
              <p className="mb-0">Get more insights</p>
            </div>
          </div>

          <div className="d-flex align-items-center justify-content-center"></div>
          <div className="result-information">
            <p>Problem Type: {problemType.displayName}</p>
            <p>Fitness Value: {fitnessValue}</p>
            <p>Used Algorithm: {usedAlgorithm}</p>
            <p>Runtime: {runtime} ms</p>
          </div>
        </div>
        <div className="view-1" style={{display: "block"}}>
          <div className="d-flex">
            <Button
                variant="success"
                size="md"
                style={{justifyContent: "center", margin: "auto", width: 150}}
                onClick={handleExportToExcel}
            >
              Export result
            </Button>
          </div>
          <h3 style={{marginBottom: 20, marginTop: 40}}>
            THE COUPLES AFTER GALE-SHAPLEY ALGORITHM s
          </h3>
          <div className="filter-container">
            <label htmlFor="setFilter">Filter by set: </label>
            <select
                id="setFilter"
                value={selectedSet}
                onChange={handleSetFilterChange}
                style={{marginLeft: 10, marginBottom: 20}}
            >
              <option value="all">All</option>
              <option value="leftovers">LEFTOVERS</option>
              {Array.from({length: appData.problem.numberOfSets}, (_, i) => (
                  <option key={`set-${i + 1}`} value={i + 1}>
                    Set {i + 1}
                  </option>
              ))}
            </select>
          </div>


          <Table striped bordered hover responsive>
            <thead>
            <tr className="table-success">
              {/* <th>#</th> */}
              <th>First Partner</th>
              <th>Second Partner</th>
              <th>Couple fitness</th>
              <th>First Partner Set</th>
              {/* Thêm cột mới */}
            </tr>
            </thead>

            <tbody>{htmlOutput}</tbody>
          </Table>

          <h3 style={{marginBottom: 20, marginTop: 40, textAlign: "center"}}>
            THE LEFTOVERS AFTER GALE-SHAPLEY ALGORITHM
          </h3>
          <Table striped bordered hover responsive>
            <thead>
            <tr className="table-danger">
              <th>No.</th>
              <th>Name</th>
              <th>Set</th>
            </tr>
            </thead>
            <tbody>{htmlLeftOvers}</tbody>
          </Table>
          {/* {console.log(appData.result.data.individuals)} */}
        </div>
      </div>
  );
}
