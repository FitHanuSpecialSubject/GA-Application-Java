import React, { useContext, useState, useEffect } from "react";
import "../../module/gameTheory/css/processing.scss";
import { useNavigate } from "react-router-dom";

import Player from "../../module/gameTheory/component/Player";
import axios from "axios";
import DataContext from "../../module/core/context/DataContext";
import NothingToShow from "../../module/core/component/NothingToShow";
import Loading from "../../module/core/component/Loading";
import ParamSettingBox from "../../module/core/component/ParamSettingBox";
import PopupContext from "../../module/core/context/PopupContext";
import { GT_ALGORITHMS } from "../../const/game_theory_const";
import { axiosErrorHandler, getBackendAddress } from "../../utils/http_utils";
export default function InputProcessingPage() {
  const navigate = useNavigate();
  const { appData, setAppData, setFavicon } = useContext(DataContext);
  const [isLoading, setIsLoading] = useState(false);
  const [algorithm, setAlgorithm] = useState("NSGAII");
  const [distributedCoreParam, setDistributedCoreParam] = useState("all");
  const [populationSizeParam, setPopulationSizeParam] = useState(1000);
  const [generationParam, setGenerationParam] = useState(100);
  const [maxTimeParam, setMaxTimeParam] = useState(5000);

  const { displayPopup } = useContext(PopupContext);
  const saveExperimentResult = (result) => {
    const config = JSON.parse(localStorage.getItem("experimentConfig"));
    if (!config) return;
    config.results = {
      algorithm: result.params.usedAlgorithm,
      populationSize: result.params.populationSizeParam,
      generation: result.params.generationParam,
      maxTime: result.params.maxTimeParam,
    };
    localStorage.setItem(
      "experimentConfig",
      JSON.stringify(config)
    );
  };
  //duc


  useEffect(() => {
    if (appData?.problem) {
      document.title = appData.problem.name;
    }
  }, [appData]);
  const handleChange = (event) => {
    setAlgorithm(event.target.value);
  };
  // navigate to home page if there is no problem data
  if (!appData || !appData.problem) {
    return <NothingToShow />;
  }

  const handleSolveNow = async () => {
    try {
      const body = {
        specialPlayer: appData.problem.specialPlayer,
        normalPlayers: appData.problem.players,
        fitnessFunction: appData.problem.fitnessFunction,
        defaultPayoffFunction: appData.problem.playerPayoffFunction,
        conflictSet: appData.problem.conflictSet,
        algorithm: algorithm,
        distributedCores: distributedCoreParam,
        populationSize: populationSizeParam,
        generation: generationParam,
        maxTime: maxTimeParam,
      };
      setFavicon("running");
      setIsLoading(true);
      const res = await axios.post(
        `${getBackendAddress()}/api/game-theory-solver`,
        body,
      );
      const usedAlgorithm = res.data.data.algorithm;

      const result = {
        data: res.data.data,
        params: {
          usedAlgorithm: usedAlgorithm,
          distributedCoreParam: distributedCoreParam,
          populationSizeParam: populationSizeParam,
          generationParam: generationParam,
          maxTimeParam: maxTimeParam,
        },
      };
      saveExperimentResult(result);
      setAppData({ ...appData, result });
      setIsLoading(false);
      navigate("/result");
    } catch (err) {
      setFavicon("error");
      const { title, message } = axiosErrorHandler(err);
      setIsLoading(false);
      displayPopup(title, message, true);
    }
  };

  return (
    <div className="input-processing-page">
      <Loading
        isLoading={isLoading}
        message="Solve your problem, please do not close this window..."
      />
      <h1 className="problem-name">{appData.problem.name}</h1>

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
      {algorithm === "PAES" && (
        <p style={{ color: "red", textAlign: "center" }}>
          Population size takes no effect for PAES algorithm
        </p>
      )}
      <div className="algo-chooser">
        <p className="algorithm-text bold">Choose an algorithm: </p>

        <select
          name=""
          id=""
          value={algorithm}
          onChange={handleChange}
          className="algorithm-select"
        >
          {GT_ALGORITHMS.map(({ displayName, value }) => (
            <option key={value} value={value}>
              {displayName}
            </option>
          ))}
        </select>
      </div>

      <p className="solve-now-btn" onClick={handleSolveNow}>
        Solve now
      </p>
      <p className="playerNum bold">
        {appData.problem.players.length}{" "}
        {appData.problem.players.length < 2 ? "Player" : "Players"}{" "}
      </p>

      <div className="player-container">
        {appData.problem.players.map((player, index) => (
          <div key={index}>
            <Player
              index={index}
              name={player.name}
              strategies={player.strategies}
            />
          </div>
        ))}
      </div>
    </div>
  );
}
