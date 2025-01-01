import React from 'react'
import "../../module/gameTheory/css/processing.scss"
import {useNavigate} from 'react-router-dom'

import {useContext, useState, useEffect} from 'react'
import Player from '../../module/gameTheory/component/Player';
import axios from 'axios';
import DataContext from "../../module/core/context/DataContext"
import NothingToShow from '../../module/core/component/NothingToShow';
import Loading from '../../module/core/component/Loading';
import ParamSettingBox from '../../module/core/component/ParamSettingBox';
import PopupContext from '../../module/core/context/PopupContext';
import {GT_ALGORITHMS} from '../../const/game_theory_const';
export default function InputProcessingPage() {
    const navigate = useNavigate();
    const {appData, setAppData} = useContext(DataContext);
    const [isLoading, setIsLoading] = useState(false);
    const [algorithm, setAlgorithm] = useState('NSGAII');
    const [distributedCoreParam, setDistributedCoreParam] = useState("all")
    const [populationSizeParam, setPopulationSizeParam] = useState(1000)
    const [generationParam, setGenerationParam] = useState(100)
    const [maxTimeParam, setMaxTimeParam] = useState(5000)

    const {displayPopup} = useContext(PopupContext)

    useEffect(() => {
        if (!appData || !appData.problem) return;
        document.title = appData.problem.name
    })
    const handleChange = (event) => {
        setAlgorithm(event.target.value);
    }
    // navigate to home page if there is no problem data
    if (!appData || !appData.problem) {
        return (
            <NothingToShow/>
        )
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
            }
            setIsLoading(true);
            console.log("MAKE a POST request to: ");
            const res = await axios.post(`http://${process.env.REACT_APP_BACKEND_URL}:${process.env.REACT_APP_BACKEND_PORT}/api/game-theory-solver`, body);
            // const reswait axios.post(`http://172.20.0.2:${process.env.REACT_APP_BACKEND_PORT}/api/game-theory-solver`, body);
            console.log(res.data.data);
            const usedAlgorithm = res.data.data.algorithm;


            const result = {
                data: res.data.data,
                params: {
                    usedAlgorithm: usedAlgorithm,
                    distributedCoreParam: distributedCoreParam,
                    populationSizeParam: populationSizeParam,
                    generationParam: generationParam,
                    maxTimeParam: maxTimeParam
                }

            }
            setAppData({...appData, result});
            setIsLoading(false);
            navigate('/result')
        } catch (err) {
            console.log(err);
            setIsLoading(false);
            displayPopup("Running failed", "Please check the dataset and try again or contact the admin!", true)
        }

    }


    return (
        <div className='input-processing-page'>
            <Loading isLoading={isLoading} message='Solve your problem, please do not close this window...'/>
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
            {
                algorithm == 'PAES' &&
                <p style={{color:"red", textAlign:"center"}}>Population size takes no effect for PAES algorithm</p>

            }
            <div className="algo-chooser">
                <p className='algorithm-text bold'>Choose an algorithm: </p>

                <select name="" id="" value={algorithm} onChange={handleChange} className='algorithm-select'>
                    {GT_ALGORITHMS.map(({displayName, value}) => (
                        <option key={value} value={value}>
                            {displayName}
                        </option>
                    ))}
                </select>
            </div>

            <p className="solve-now-btn" onClick={handleSolveNow}>Solve now</p>
            <p className="playerNum bold">{appData.problem.players.length} {appData.problem.players.length < 2 ? 'Player' : "Players"}  </p>

            <div className="player-container">
                {appData.problem.players.map((player, index) => (
                    <div key={index}>
                        <Player index={index} name={player.name} strategies={player.strategies}/>
                    </div>
                ))}
            </div>
        </div>
    )
}
