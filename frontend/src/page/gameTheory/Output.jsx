import React from 'react'
import "../../module/gameTheory/css/output.scss"
import PlayerResult from '../../module/gameTheory/component/PlayerResult'
import ExcelImage from '../../module/core/asset/image/excel.png'
import GraphImage from '../../module/core/asset/image/graph.png'
import { useContext, useState } from 'react'
import DataContext from "../../module/core/context/DataContext"
import { useNavigate } from 'react-router-dom';
import NothingToShow from '../../module/core/component/NothingToShow';
import Loading from '../../module/core/component/Loading';
import * as XLSX from '@e965/xlsx';
import { saveAs } from 'file-saver';
import Popup from '../../module/core/component/Popup';
import axios from 'axios'
import ParamSettingBox from '../../module/core/component/ParamSettingBox';
import PopupContext from '../../module/core/context/PopupContext'
import { createSystemInfoSheet, createParameterConfigSheet , loadProblemDataOld, loadProblemDataParallel } from '../../utils/excel_utils.js';


import SockJS from 'sockjs-client';
import { v4 } from 'uuid';
import { over } from 'stompjs';

let stompClient = null
export default function OutputPage() {
  const navigate = useNavigate();
  const { appData, setAppData } = useContext(DataContext)
  const [isLoading, setIsLoading] = useState(false);
  const [isShowPopup, setIsShowPopup] = useState(false);
  const { displayPopup } = useContext(PopupContext)
  const [sessionCode] = useState(v4())
  const [loadingMessage, setLoadingMessage] = useState("Processing to get problem insights, please wait...")
  const [loadingEstimatedTime, setLoadingEstimatedTime] = useState(null)
  const [loadingPercentage, setLoadingPercentage] = useState()
  const [distributedCoreParam, setDistributedCoreParam] = useState("all")
  const [populationSizeParam, setPopulationSizeParam] = useState(1000)
  const [generationParam, setGenerationParam] = useState(100)
  const [maxTimeParam, setMaxTimeParam] = useState(5000)

  if (appData == null) {
    return (
      <NothingToShow />
    )
  }

  const handleExportToExcel = async () => {
    const workbook = XLSX.utils.book_new();
    // write result data to sheet 1
    const sheet1 = XLSX.utils.aoa_to_sheet([
      ["Fitness value", appData.result.data.fitnessValue],
      ["Used algorithm", appData.result.params.usedAlgorithm],
      ["Runtime (in seconds)", appData.result.data.runtime],
      ["Player name", "Choosen strategy name", "Payoff value"],
    ]);

    // append players data to sheet 1
    appData.result.data.players.forEach(player => {
      const row = [player.playerName, player.strategyName, player.payoff];
      XLSX.utils.sheet_add_aoa(sheet1, [row], { origin: -1 });
    })


    // write parameter configurations to sheet 2
    const sheet2 = createParameterConfigSheet(appData);


    // write computer specs to sheet 3
    const sheet3 = createSystemInfoSheet(appData);


    // append sheets to workbook
    XLSX.utils.book_append_sheet(workbook, sheet1, 'Optiomal solution');
    XLSX.utils.book_append_sheet(workbook, sheet2, 'Parameter Configurations');
    XLSX.utils.book_append_sheet(workbook, sheet3, 'Computer Specifications');


    // write workbook to file
    const wbout = await XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([wbout], { type: 'application/octet-stream' });
    saveAs(blob, 'result.xlsx');
  };

  const handleGetMoreInsights = () => {
    setIsShowPopup(true);
  }

  const handlePopupOk = async () => {
    try {
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
      }
      
      setIsLoading(true);
      await connectWebSocket() // connect to websocket to get the progress percentage
      const res = await axios.post(`http://${process.env.REACT_APP_BACKEND_URL}:${process.env.REACT_APP_BACKEND_PORT}/api/problem-result-insights/${sessionCode}`, body);
      setIsLoading(false);

      const insights = {
        data: res.data.data,
        params: {
          distributedCoreParam: distributedCoreParam,
          populationSizeParam: populationSizeParam,
          generationParam: generationParam,
          maxTimeParam: maxTimeParam,
        }
      }
      setAppData({ ...appData, insights });
      closeWebSocketConnection()
      navigate('/insights') // navigate to insights page
    } catch (err) {
      setIsLoading(false);
      displayPopup("Something went wrong!", "Get insights failed!, please contact the admin!", true)
    }

  }

  const connectWebSocket = async () => {
    let Sock = new SockJS(`http://${process.env.REACT_APP_BACKEND_URL}:${process.env.REACT_APP_BACKEND_PORT}/ws`);
    stompClient = over(Sock);
    await stompClient.connect({}, onConnected, onError);
  }
  const onConnected = () => {
    stompClient.subscribe('/session/' + sessionCode + '/progress', onPrivateMessage);
    console.log('Connected to websocket server!');
  }

  const onError = (err) => {
    console.log(err);
    // displayPopup("Something went wrong!", "Connect to server failed!, please contact the admin!", true)
  }

  const closeWebSocketConnection = () => {
    if (stompClient) {
      stompClient.disconnect();
    }
  }

  const onPrivateMessage = (payload) => {
    let payloadData = JSON.parse(payload.body);


    // some return data are to show the progress, some are not
    // if the data is to show the progress, then it will have the estimated time and percentage
    if (payloadData.inProgress) {
      setLoadingEstimatedTime(payloadData.minuteLeft)
      setLoadingPercentage(payloadData.percentage)
    } 

    setLoadingMessage(payloadData.message)

  }



  return (
    <div className='output-page'>
      <Popup
        isShow={isShowPopup}
        setIsShow={setIsShowPopup}
        title={"Get detailed insights"}
        // message={`This process can take estimated ${data.estimatedWaitingTime || 1} minute(s) and you will be redirected to another page. Do you want to continue?`}
        message={`This process can take a while do you to continue?`}
        okCallback={handlePopupOk}
      />

      {/* <Loading isLoading={isLoading} message={`Get more detailed insights. This can take estimated ${data.estimatedWaitingTime || 1} minute(s)...`} /> */}
      <Loading isLoading={isLoading}
        percentage={loadingPercentage}
        estimatedTime={loadingEstimatedTime}
        message={loadingMessage} />
      <h1 className="problem-name">{appData.problem.name}</h1>
      <br />
      <p className='below-headertext'>Optimal solution</p>
      <div className="output-container">
        <div className="row">
          <div className="btn" onClick={handleExportToExcel}>
            <p>Export to Excel</p>
            <img src={ExcelImage} alt="" />
          </div>
        </div>
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
            <p>Get more insights</p>
            <img src={GraphImage} alt="" />
          </div>
        </div>

      </div>
      <br />
      <p className='below-headertext'> Fitness value: {appData.result.data.fitnessValue}</p>
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
  )
}
