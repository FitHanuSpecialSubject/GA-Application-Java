import React from 'react';
import '../../module/stableMatching/css/processing.scss';
import {useNavigate} from 'react-router-dom';
import {useContext, useState, useEffect} from 'react';
import axios, {AxiosError} from 'axios';
import DataContext from '../../module/core/context/DataContext';
import NothingToShow from '../../module/core/component/NothingToShow';
import Loading from '../../module/core/component/Loading';
import ParamSettingBox from '../../module/core/component/ParamSettingBox';
import PopupContext from '../../module/core/context/PopupContext';
import {SMT, SMT_VALIDATE} from '../../consts';
import {getBackendAddress} from '../../utils/http_utils';
import {SMT_ALGORITHMS} from '../../const/matching_const';

export default function InputProcessingPage() {
  const navigate = useNavigate();
  const {appData, setAppData} = useContext(DataContext);
  const [isLoading, setIsLoading] = useState(false);
  const [algorithm, setAlgorithm] = useState(SMT.DEFAULT_ALGORITHM);
  const [distributedCoreParam, setDistributedCoreParam] = useState(SMT.DEFAULT_CORE_NUM);
  const [problemType, setProblemType] = useState(SMT.PROBLEM_TYPES.MTM);
  const [problemTypeOrdinal, setProblemTypeOrdinal] = useState(SMT.PROBLEM_TYPES.MTM.ordinal);
  const [populationSizeParam, setPopulationSizeParam] = useState(SMT.DEFAULT_POPULATION_SIZE);
  const [generationParam, setGenerationParam] = useState(SMT.DEFAULT_GENERATION_NUM);
  const [maxTimeParam, setMaxTimeParam] = useState(SMT.DEFAULT_MAXTIME);
  const {displayPopup} = useContext(PopupContext);
  const [body, setBody] = useState(null);

  useEffect(() => {
    if (appData && appData.problem) {
      document.title = "Special Subject Solver";
    }
  }, [appData?.problem]);

  const handleChange = (event) => {
    setAlgorithm(event.target.value);
  };

  // Hàm thay đổi problemType
  const handleChangeProblemType = (event) => {
    let ordinal = Number(event.target.value);
    for (const key in SMT.PROBLEM_TYPES) {
      if (SMT.PROBLEM_TYPES[key].ordinal === ordinal) {
        setProblemTypeOrdinal(ordinal);
        setProblemType(SMT.PROBLEM_TYPES[key]);
        // Cập nhật appData để lưu lại thông tin problemType
        setAppData((prevData) => ({
          ...prevData,
          problemTypeOrdinal: ordinal,
          problemType: SMT.PROBLEM_TYPES[key],  // Lưu loại bài toán vào appData
        }));
        return;
      }
    }
    //setProblemType(SMT.PROBLEM_TYPES.OTO);
  };

  // navigate to home page if there is no problem data
  if (!appData || !appData.problem) {
    return <NothingToShow/>;
  }

  const handleSolveNow = async () => {
    try {
      if (!appData || !appData.problem) {
        displayPopup('Error', 'Stable Matching Problem data is missing.', true);
        return;
      }

      const evaluateFunctions = appData.problem.evaluateFunctions || [];
      for (const func of evaluateFunctions) {
        for (const keyword of SMT_VALIDATE.INVALID_MATH_SYMBOLS) {
          if (func.includes(keyword)) {
            return displayPopup('Invalid Evaluate Function(s)',
                `Evaluate function (${func}) contains invalid symbol (${keyword})`,
                true);
          }
        }
      }
      // Validate fitness func
      for (const keyword of SMT_VALIDATE.INVALID_MATH_SYMBOLS) {
        if (appData.problem.fitnessFunction.includes(keyword)) {
          return displayPopup('Invalid Evaluate Function(s)',
              `Fitness function (${appData.problem.fitnessFunction}) contains invalid symbol (${keyword})`,
              true);
        }
      }
      const requestBody = {
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
        excludePairs: appData.problem.excludePairs,
        evaluateFunctions: evaluateFunctions,
        algorithm: algorithm,
        distributedCores: distributedCoreParam,
        populationSize: populationSizeParam,
        generation: generationParam,
        maxTime: maxTimeParam,
      };

      let serviceEndpoint = problemType.endpoint;
      let endpoint = `${getBackendAddress()}${serviceEndpoint}`;
      console.log(endpoint);

      setIsLoading(true);
      console.log(requestBody);
      const res = await axios.post(endpoint, requestBody);

      console.log(res.data.data);
      const runtime = res.data.data.runtime;
      const usedAlgorithm = res.data.data.algorithm;

      const result = {
        data: res.data.data,
        params: {
          runtime: runtime,
          usedAlgorithm: usedAlgorithm,
          distributedCoreParam: distributedCoreParam,
          populationSizeParam: populationSizeParam,
          generationParam: generationParam,
          maxTimeParam: maxTimeParam,
        },
      };

      setAppData({
        ...appData,
        result,
        problemType: problemType
      });

      setIsLoading(false);
      console.log(result);
      navigate('/matching-theory/result');

    } catch (err) {
      console.error(err);
      //Handle Errors
      if (err instanceof AxiosError) {

        let title;
        let message;

        if (err.response) {
          title = `Server responded with status ${err.response.status}`;
          message = (
            <div>
              <strong>Error: </strong>{err.response.data.error}
              <br />
              <strong>Chi tiết lỗi: </strong>{err.response.data.message}
            </div>
          );
        } else if (err.request) {
          title = 'No response received';
          message = 'Server maybe down at the moment!';
        } else {
          title = 'Error setting up request';
          message = `${err.message}`;
        }
        setIsLoading(false);
        displayPopup(title, message, true);

      } else {

        setIsLoading(false);
        displayPopup('Running failed',
            'An unknown error occurred! Please contact the admin!', true);

      }
    }
  };

  //Add demo individuals for displaying
  const demoIndividuals = [];
  const defaultDisNum = SMT.DEFAULT_SAMPLE_DISPLAY_NUM;
  const numDemo = (appData.problem.numberOfIndividuals > defaultDisNum)
      ? defaultDisNum
      : appData.problem.numberOfIndividuals;
  const problem = appData.problem;

    for (let i = 0; i < numDemo; i++) {
      demoIndividuals.push(
          {
            name: problem.individualNames[i],
            set: problem.individualSetIndices[i],
            capacity: problem.individualCapacities[i],
            property: `P: ${problem.individualProperties[i].join(' | ')} <br/>
                       W: ${problem.individualWeights[i].join(' | ')} <br/>
                       R: ${problem.individualRequirements[i].join(' | ')}`
          },
      );
    }

  return (
      <div className="input-processing-page">
        <Loading isLoading={isLoading}
                 message="Solve your problem, please do not close this window..."/>
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
            algorithm === 'PAES' &&
            <p style={{color:"red", textAlign:"center"}}>Population size takes no effect for PAES
              algorithm</p>
        }

        {/* Lựa chọn Problem type */}
        <div className="problem-type-chooser">
          <p className="problem-type-text bold">Choose a problem type: </p>
          <select
              value={problemTypeOrdinal}
              onChange={handleChangeProblemType}
              className="problem-type-select"
          >
            {Object.values(SMT.PROBLEM_TYPES).map(type => (
                <option key={type.ordinal} value={type.ordinal}>
                  {type.displayName}
                </option>
            ))}
          </select>
        </div>

        <div className="algo-chooser">
          <p className="algorithm-text bold">Choose an algorithm: </p>

          {/*drop down chọn thuật toán*/}
          <select name="algorithm"
                  value={algorithm}
                  onChange={handleChange}
                  className="algorithm-select"
          >
            {SMT_ALGORITHMS.map(({displayName, value}) => (
                <option key={value} value={value}>
                  {displayName}
                </option>
            ))}
          </select>
        </div>

        <div>
          <p className="solve-now-btn" onClick={handleSolveNow}>
            Solve now
          </p>
          {body && (
              <div>
                <h3>JSON Data to backend:</h3>
                <pre
                    style={{
                      whiteSpace: 'pre-wrap',
                      maxWidth: '800px',
                      overflowX: 'auto',
                    }}
                >
                        {JSON.stringify(body, null, 2)}
                    </pre>
              </div>
          )}
        </div>

        {/* Hiển thị thông tin */}
        <div className="info-display">
          <h3>Information:</h3>
          <table className="info-table">
            <tbody>
            <tr>
              <td><strong>Name</strong></td>
              <td>{appData.problem.nameOfProblem}</td>
            </tr>
            <tr>
              <td><strong>Number of sets</strong></td>
              <td>{appData.problem.numberOfSets}</td>
            </tr>
            <tr>
              <td><strong>Number of individuals</strong></td>
              <td>{appData.problem.numberOfIndividuals}</td>
            </tr>
            <tr>
              <td><strong>Attributes</strong></td>
              {/*<td>{appData.problem.characteristics.join(', ')}</td>*/}
              <td>
                <ol>
                {appData.problem.characteristics.map((elm, idx) => (
                  <li key={idx}>{String(elm)}</li>
                ))}
                </ol>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        {/* Bảng hiển thị dữ liệu Individuals */}
        <div className="individuals-table-container">
          <h2>Individuals Data</h2>
          <table className="individuals-table">
            <thead>
            <tr>
              <th>Name</th>
              <th>Set Type</th>
              <th>Capacity</th>
              <th>Properties</th>
            </tr>
            </thead>
            <tbody>
            {demoIndividuals && demoIndividuals.map((elm, index) => (
                <tr key={index}>
                  <td>{elm.name}</td>
                  <td>{elm.set}</td>
                  <td>{elm.capacity}</td>
                  <td>{elm.property}</td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
  );
}