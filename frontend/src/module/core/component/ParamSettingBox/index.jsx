import React from 'react';
import "./style.scss";
import InputHint from '../InputHint';
import { useState, useContext } from 'react';
import Param from '../Param';

export default function Input({ distributedCoreParam, setDistributedCoreParam, populationSizeParam, setPopulationSizeParam, generationParam, setGenerationParam, maxTimeParam, setMaxTimeParam }) {
    // const playerHolder = error ? message: message
    const [showHint, setShowHint] = useState(false);

    const handleMouseOver = () => {
        setShowHint(true);

    }
    const handleMouseLeave = () => {
        setShowHint(false);

    }


   

    return (
        <>
            <div className='param-setting-box'>

                <div className="distributed-core-param">
                    <InputHint
                        showHint={showHint}
                        setShowHint={setShowHint}
                        heading="Number of CPU cores to utilize"
                        description="The number of CPU cores or processing units on which you want to distribute the computational workload. Utilizing parallel processing to maximize the efficiency"
                        guideSectionIndex={8}
                    />
                    <div className='text'>
                        <i className="info fa-solid fa-info" onMouseOver={handleMouseOver} onMouseLeave={handleMouseLeave}></i>
                        <p className="bold">Number of distributed processing CPU cores:</p>
                    </div>
                    <select name="" id="" value={distributedCoreParam} onChange={e => setDistributedCoreParam(e.target.value)}>
                        <option value="2">2 cores</option>
                        <option value="4">4 cores</option>
                        <option value="8">8 cores</option>
                        <option value="all">All available cores</option>
                    </select>
                </div>


                <Param
                    inputText="Define the population size:"
                    hintTitle={"The population size in optimization algorithms"}
                    hintContent={"The number of individuals (solutions) considered at each iteration. A larger population size promotes exploration and diversity, potentially leading to better optimization results but also increased computational requirements."}
                    paramVal={populationSizeParam}
                    setParamVal={setPopulationSizeParam}
                />
                <Param
                    inputText="Enter the number of crossover generations:"
                    hintTitle={"The number of crossover generations"}
                    hintContent={"The number of crossover generations. A larger number of generations promotes exploration and diversity, potentially leading to better optimization results but also increased computational requirements."}
                    paramVal={generationParam}
                    setParamVal={setGenerationParam}
                />
                <Param
                    inputText="Maximum execution time in millseconds:"
                    hintTitle={"Optimization execution max time"}
                    hintContent={"Max time sets a maximum duration for the optimization process, ensuring it terminates within a specified time limit. It helps control the execution time and prevents prolonged optimization runs, allowing for efficient utilization of computational resources"}
                    paramVal={maxTimeParam}
                    setParamVal={setMaxTimeParam}
                />
            </div>




        </>

    )
}