import React, { useEffect } from "react";
import "../../module/gameTheory/css/output.scss";
import PlayerResult from "../../module/gameTheory/component/PlayerResult";
import { useContext, useState } from "react";
import DataContext from "../../module/core/context/DataContext";
import { useNavigate } from "react-router-dom";
import NothingToShow from "../../module/core/component/NothingToShow";
import Loading from "../../module/core/component/Loading";
import { saveAs } from "file-saver";
import Popup from "../../module/core/component/Popup";
import axios from "axios";
import ParamSettingBox from "../../module/core/component/ParamSettingBox";
import PopupContext from "../../module/core/context/PopupContext";
import {
  createSystemInfoSheet,
  createParameterConfigSheet,
} from "../../utils/excel_utils.js";

import SockJS from "sockjs-client";
import { v4 } from "uuid";
import { over } from "stompjs";
import ExcelJS from "exceljs";
import { RESULT_WORKBOOK } from "../../const/excel_const";
import { getBackendAddress } from "../../utils/http_utils";
import { FaChartLine, FaRegFileExcel } from "react-icons/fa6";

let stompClient = null;
export default function OutputPage() {
  const navigate = useNavigate();
  const { appData, setAppData, setFavicon } = useContext(DataContext);
  const [isLoading, setIsLoading] = useState(false);
  const [isShowPopup, setIsShowPopup] = useState(false);
  const { displayPopup } = useContext(PopupContext);
  const [sessionCode] = useState(v4());
  const [loadingMessage, setLoadingMessage] = useState(
    "Processing to get problem insights, please wait...",
  );
  const [loadingEstimatedTime, setLoadingEstimatedTime] = useState(null);
  const [loadingPercentage, setLoadingPercentage] = useState();
  const [distributedCoreParam, setDistributedCoreParam] = useState("all");
  const [populationSizeParam, setPopulationSizeParam] = useState(1000);
  const [generationParam, setGenerationParam] = useState(100);
  const [maxTimeParam, setMaxTimeParam] = useState(5000);
  const [runCountParam, setRunCountParam] = useState(10);

  if (appData == null) {
    return <NothingToShow />;
  }
  useEffect(() => {
    setFavicon("success");
    const socket = new SockJS(getBackendAddress() + "/ws");
    stompClient = over(socket);
    stompClient.connect({}, () => {
      console.log("Connected to WebSocket");
      stompClient.subscribe("/topic/progress/" + sessionCode, (message) => {
        const data = JSON.parse(message.body);
        setLoadingPercentage(data.percentage);
        setLoadingMessage(data.message);
        setLoadingEstimatedTime(data.estimatedTime);
      });
    });
    return () => {
      if (stompClient) {
      stompClient.disconnect();
      }
    };
  }, []);
//duc
  const handleExportToExcel = async () => {
    const workbook = new ExcelJS.Workbook();
    // write result data to sheet 1
    const sheet1 = workbook.addWorksheet(RESULT_WORKBOOK.SOLUTION_SHEET_NAME);
    sheet1.addRows([
      ["Fitness value", appData.result.data.fitnessValue],
      ["Used algorithm", appData.result.params.usedAlgorithm],
      ["Runtime (in seconds)", appData.result.data.runtime],
      ["Player name", "Choosen strategy name", "Payoff value"],
    ]);

    // append players data to sheet 1
    appData.result.data.players.forEach((player) => {
      const row = [player.playerName, player.strategyName, player.payoff];
      sheet1.addRow(row);
    });

    // write parameter configurations to sheet 2
    createParameterConfigSheet(workbook, appData);
    // write computer specs to sheet 3
    createSystemInfoSheet(workbook, appData);
    // write workbook to file
    const wbout = await workbook.xlsx.writeBuffer();
    const blob = new Blob([wbout], { type: "application/octet-stream" });
    saveAs(blob, appData.problem.name + "_Result.xlsx");
  };

  const handleGetMoreInsights = () => {
    setIsShowPopup(true);
  };

  const handlePopupOk = async () => {
    try {
      setFavicon("running");
      setIsShowPopup(false);
      const body = {
        specialPlayer: appData.problem.specialPlayer,
        normalPlayers: appData.problem.players,
        fitnessFunction: appData.problem.fitnessFunction,
        defaultPayoffFunction: appData.problem.playerPayoffFunction,
        conflictSet: appData.problem.conflictSet,
        distributedCores: distributedCoreParam,
        populationSize: populationSizeParam,
        generation: generationParam,
        maxTime: maxTimeParam,
        runCountPerAlgorithm: runCountParam,
      };

      setIsLoading(true);
      await connectWebSocket(); // connect to websocket to get the progress percentage
      const res = await axios.post(
        `${getBackendAddress()}/api/problem-result-insights/${sessionCode}`,
        body,
      );
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
      setFavicon("success");
      navigate("/insights"); // navigate to insights page
    } catch (err) {
      setFavicon("error");
      console.error(err);
      setIsLoading(false);
      displayPopup(
        "Something went wrong!",
        "Get insights failed!, please contact the admin!",
        true,
      );
    }
  };

  const connectWebSocket = async () => {
    const Sock = new SockJS(`${getBackendAddress()}/ws`);
    stompClient = over(Sock);
    await stompClient.connect({}, onConnected, onError);
  };
  const onConnected = () => {
    stompClient.subscribe(
      "/session/" + sessionCode + "/progress",
      onPrivateMessage,
    );
  };

  const onError = (err) => {
    console.error(err);
    // displayPopup("Something went wrong!", "Connect to server failed!, please contact the admin!", true)
  };

  const closeWebSocketConnection = () => {
    if (stompClient) {
      stompClient.disconnect();
    }
  };

  const onPrivateMessage = (payload) => {
    const payloadData = JSON.parse(payload.body);

    // some return data are to show the progress, some are not
    // if the data is to show the progress, then it will have the estimated time and percentage
    if (payloadData.inProgress) {
      setLoadingEstimatedTime(payloadData.minuteLeft);
      setLoadingPercentage(payloadData.percentage);
    }

    setLoadingMessage(payloadData.message);
  };

  return (
    <div className="output-page">
      <Popup
        isShow={isShowPopup}
        setIsShow={setIsShowPopup}
        title={"Get detailed insights"}
        message={`This process can take a while do you to continue?`}
        okCallback={handlePopupOk}
      />

      <Loading
        isLoading={isLoading}
        percentage={loadingPercentage}
        estimatedTime={loadingEstimatedTime}
        message={loadingMessage}
      />
      <h1 className="problem-name">{appData.problem.name}</h1>
      <br />
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
            runCountParam={runCountParam}
            setRunCountParam={setRunCountParam}
          />
          <div
            className="align-self-center btn btn-outline-primary d-flex justify-content-center border-1 p-3"
            onClick={handleGetMoreInsights}
          >
            <FaChartLine className="me-0 fs-4" />
            Get more insights
          </div>
        </div>
      </div>
      <div
        className="btn align-self-center mb-3 btn-success d-flex justify-content-center border-1 p-3"
        onClick={handleExportToExcel}
      >
        <FaRegFileExcel className="me-0 fs-4" />
        Get Excel Template
      </div>
      <p className="below-headertext">
        {" "}
        Fitness value: {appData.result.data.fitnessValue}
      </p>
      <br />

      <div className="table-container">
        <div className="grid-container">
          <div className="column head-column">No</div>
          <div className="column head-column">Player Name</div>
          <div className="column head-column">Choosen strategy name</div>
          <div className="column head-column">Payoff value</div>
        </div>

        {appData.result.data.players?.map((player, index) => (
          <PlayerResult key={index} player={player} index={index + 1} />
        ))}
      </div>
    </div>
  );
}
