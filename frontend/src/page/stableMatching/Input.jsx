import React, {useContext, useEffect, useState} from 'react';
import Input from '../../module/core/component/input';
import ExcelImage from '../../module/core/asset/image/excel.png';
import {saveAs} from 'file-saver';
import * as XLSX from '@e965/xlsx';
import DataContext from '../../module/core/context/DataContext';
import ExcelJS from 'exceljs';
import {Link, useNavigate} from 'react-router-dom';
import '../../module/stableMatching/css/input.scss';

import Loading from '../../module/core/component/Loading';
import PopupContext from '../../module/core/context/PopupContext';
import {SMT} from '../../consts';
import {validateExcelFile} from '../../utils/file_utils';
import {
  loadProblemDataParallel,
  loadExcludePairs
} from '../../utils/excel_utils';
import Checkbox from '../../module/core/component/Checkbox';

export default function InputPage() {
  //initialize from data
  const [excelFile, setExcelFile] = useState(null);
  const [problemName, setProblemName] = useState('');
  const [setNum, setSetNum] = useState(undefined);
  const [characteristicsNum, setCharacteristicsNum] = useState(undefined);
  const [totalIndividualsNum, setTotalIndividualsNum] = useState(undefined);
  const [fitnessFunction, setFitnessFunction] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const [excelFileError, setExcelFileError] = useState('');
  const [problemNameError, setProblemNameError] = useState('');
  const [setNumError, setSetNumError] = useState('');
  const [characteristicsNumError, setCharacteristicsNumError] = useState('');
  const [totalIndividualsNumError, setTotalIndividualsNumError] = useState('');
  const [fitnessFunctionError, setFitnessFunctionError] = useState('');

  const [setCharacteristics, setSetCharacteristics] = useState('');
  const {setAppData, setGuideSectionIndex} = useContext(DataContext);
  const {displayPopup} = useContext(PopupContext);
  const [colNums, setColNums] = useState(0);
  const [setEvaluateFunction, setSetEvaluateFunction] = useState(
      Array.from({length: colNums}, () => ''));
  const [setIndividuals, setSetIndividuals] = useState(
      Array.from({length: colNums}, () => ''));

  const [setMany, setSetMany] = useState(
      Array.from({length: colNums}, () => false));
  const navigate = useNavigate();

  // useEffect to validate and read file when it changes
  useEffect(() => {
    if (excelFile) {
      try {
        if (validateExcelFile(excelFile)) {
          readExcelFile(excelFile);
        } else {
          displayPopup('Something went wrong!',
              'The file was not an Excel file!', true);
          setExcelFileError('The file was not an Excel file!');
        }
      } catch (error) {
        console.error(error);
        displayPopup('Error', error.message, true);
      }
    }
  }, [excelFile]);

  // Function to read data from the Excel file
  const readExcelFile = async (file) => {
    const reader = new FileReader();
    try {
      reader.onload = async (e) => {
        const data = e.target.result;
        const workbook = XLSX.read(data, {type: 'binary'});

        let problemInfo = await loadProblemDataParallel(workbook,
            SMT.INDIVIDUAL_SHEET);
        let excludePairs = await loadExcludePairs(workbook,
            SMT.EXCLUDE_PAIRS_SHEET);
        setAppData({
          problem: {
            nameOfProblem: problemInfo.problemName,
            numberOfSets: problemInfo.setNum,
            setNames: problemInfo.setNames,
            setTypes: problemInfo.setTypes,
            numberOfIndividuals: problemInfo.totalNumberOfIndividuals,
            individualNames: problemInfo.individualNames,
            characteristics: problemInfo.characteristics,
            individualSetIndices: problemInfo.individualSetIndices,
            individualCapacities: problemInfo.individualCapacities,
            individualProperties: problemInfo.individualProperties,
            individualRequirements: problemInfo.individualRequirements,
            individualWeights: problemInfo.individualWeights,
            individuals: problemInfo.individuals,
            fitnessFunction: problemInfo.fitnessFunction,
            evaluateFunctions: problemInfo.setEvaluateFunction,
            excludePairs
          }
        })
        navigate('/matching-theory/input-processing');
      };
      reader.readAsBinaryString(file);
    } catch (error) {
      console.error(error);
      setIsLoading(false);
      displayPopup('Something went wrong!',
          'Check the input file again for contact the admin!', true);
    }
  };

  const handleGetExcelTemplate = () => {
    if (validateForm()) {
      downloadExcel();
    }
  };

  const validateForm = () => {
    let error = false;
    const maxSets = 10; // Số lượng tập tối đa
    const maxCharacteristics = 15; // Số lượng đặc điểm tối đa
    const maxTotalIndividuals = 100; // Số lượng cá nhân tối đa

    const validFunctionPattern = /^[a-zA-Z0-9\s\+\-\*\/\^\(\)]+$/;
    // check if the problem name is empty
    if (!problemName) {
      setProblemNameError('Problem name must not be empty');
      error = true;
    } else {
      setProblemNameError('');
    }

    // check if the number of set is empty
    if (!setNum) {
      setSetNumError('Number of set must not be empty');
      error = true;
    } else {
      setSetNumError('');
    }

    // check if the number of characteristics is empty
    if (!characteristicsNum) {
      setCharacteristicsNumError('Number of characteristics must not be empty');
      error = true;
    } else {
      setCharacteristicsNumError('');
    }

    // check if the number of total individuals is empty
    if (!totalIndividualsNum) {
      setTotalIndividualsNumError(
          'Number of total individuals must not be empty');
      error = true;
    } else {
      setTotalIndividualsNumError('');
    }

    // check if the number of strategies is empty
    if (!fitnessFunction) {
      setFitnessFunctionError('Fitness function must not be empty');
      error = true;
    } else {
      setFitnessFunctionError('');
    }
    // Kiểm tra số lượng tập
    if (!setNum || setNum > maxSets) {
      setSetNumError(`Number of set must be from 1 to ${maxSets}`);
      error = true;
    } else {
      setSetNumError('');
    }

    // Kiểm tra số lượng đặc điểm
    if (!characteristicsNum || characteristicsNum > maxCharacteristics) {
      setCharacteristicsNumError(
          `The number of characteristics must be from 1 to ${maxCharacteristics}`);
      error = true;
    } else {
      setCharacteristicsNumError('');
    }

    // Kiểm tra số lượng cá nhân
    if (!totalIndividualsNum || totalIndividualsNum > maxTotalIndividuals) {
      setTotalIndividualsNumError(
          `The number of individuals must be from 1 to ${maxTotalIndividuals}`);
      error = true;
    } else {
      setTotalIndividualsNumError('');
    }

    //fitness
    if (!fitnessFunction || !validFunctionPattern.test(fitnessFunction)) {
      setFitnessFunctionError('Function value contains an invalid character');
      error = true;
    } else {
      setFitnessFunctionError('');
    }
    setEvaluateFunction.forEach((evaluateFunction, index) => {
      if (!evaluateFunction || !validFunctionPattern.test(evaluateFunction)) {
        setEvaluateFunction(prevState => {
          const newState = [...prevState];
          newState[index] = 'Function value contains an invalid character';
          return newState;
        });
        error = true;
      }
    });
    // if there is no error, return true
    return !error;
  };

  const downloadExcel = async () => {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('Problem Information');
    const excludePairsWorksheet  = workbook.addWorksheet("Exclude Pairs");
    const guidelinesWorksheet = workbook.addWorksheet('Guidelines');

    // Add "Problem Information" worksheet
    worksheet.addRow(['Problem name', problemName]);
    worksheet.addRow(['Number of set', setNum]);
    worksheet.addRow(['Number of individuals', totalIndividualsNum]);
    worksheet.addRow(['Number of characteristics', characteristicsNum]);
    worksheet.addRow(['Fitness function', fitnessFunction]);

    // Add evaluate function sets
    setEvaluateFunction.forEach((evaluateFunction, index) => {
      worksheet.addRow(
          [`Evaluate Function Set_${index + 1}`, evaluateFunction]);
    });
    for (let i = 0; i < Number(setNum); i++) {
      const numberSetIndividuals = Number(setIndividuals[i]);
      if (i === 0) {
        const row8 = ['Set_1'];
        if (setMany[i] === true) {
          row8.push('Set Many');
        } else {
          row8.push('Set One');
        }
        row8.push('Capacity');
        row8.push(numberSetIndividuals);
        for (let j = 0; j < Number(characteristicsNum); j++) {
          row8.push(`Characteristic_${j + 1}`);
        }

        worksheet.addRow(row8);
        for (let k = 0; k < numberSetIndividuals; k++) {
          const rowIndividual = [`Individual_${k + 1}`];
          if (setMany[i] === true) {
            rowIndividual.push(null);
            //Change
            rowIndividual.push(1);
          } else {
            rowIndividual.push(null);
            //Change
            rowIndividual.push('Fill capacity > 0');
          }
          rowIndividual.push(`Requirements`);
          const rowWeights = [null];
          rowWeights.push(null);
          rowWeights.push(null);
          rowWeights.push('Weights');
          const rowProperties = [null];
          rowProperties.push(null);
          rowProperties.push(null);
          rowProperties.push('Properties');

          for (let h = 0; h < characteristicsNum; h++) {
            rowIndividual.push(String(`req_${h + 1}`));
            rowWeights.push(String(`w_${h + 1}`));
            rowProperties.push(String(`p_${h + 1}`));
          }

          worksheet.addRow(rowIndividual);
          worksheet.addRow(rowWeights);
          worksheet.addRow(rowProperties);
        }
      } else {
        const rowSet = [`Set_${i + 1}`];
        if (setMany[i] === true) {
          rowSet.push('Set Many');
        } else {
          rowSet.push('Set One');
        }
        rowSet.push(null);
        rowSet.push(numberSetIndividuals);
        worksheet.addRow(rowSet);
        for (let k = 0; k < numberSetIndividuals; k++) {
          const rowIndividual = [`Individual_${k + 1}`];
          if (setMany[i] === true) {
            rowIndividual.push(null);
            //Change
            rowIndividual.push(1);
          } else {
            rowIndividual.push(null);
            //Change
            rowIndividual.push('Fill capacity > 0');
          }
          rowIndividual.push(`Requirements`);
          const rowWeights = [null];
          rowWeights.push(null);
          rowWeights.push(null);
          rowWeights.push('Weights');
          const rowProperties = [null];
          rowProperties.push(null);
          rowProperties.push(null);
          rowProperties.push('Properties');

          for (let h = 0; h < characteristicsNum; h++) {
            rowIndividual.push(String(`req_${h + 1}`));
            rowWeights.push(String(`w_${h + 1}`));
            rowProperties.push(String(`p_${h + 1}`));
          }

          worksheet.addRow(rowIndividual);
          worksheet.addRow(rowWeights);
          worksheet.addRow(rowProperties);
        }
      }
    }

    // Add header to Exclude Pairs
    excludePairsWorksheet.getCell('A1').value = 'Individual';
    excludePairsWorksheet.getCell('B1').value = 'Excluded from';

    // Add value to header row and merge cells
    guidelinesWorksheet.mergeCells('A1:C1');
    const headerCell = guidelinesWorksheet.getCell('A1');
    headerCell.value = 'HƯỚNG DẪN CÁCH DÙNG FILE INPUT MATCHING THEORY';

    // Set background color to light blue
    headerCell.fill = {
      type: 'pattern', pattern: 'solid', fgColor: {argb: 'ADD8E6'}, // Light blue color code
    };

    // Center the text in the merged cells
    headerCell.alignment = {vertical: 'middle', horizontal: 'center'};

    // Adjust column widths
    guidelinesWorksheet.getColumn('A').width = 25; // Set the width of column A to 15
    guidelinesWorksheet.getColumn('B').width = 100; // Set the width of column B to 15
    guidelinesWorksheet.getColumn('C').width = 25; // Set the width of column C to 15

    // Add values to the second row
    const cellValues = ['TÊN Ô', 'CHỨC NĂNG', 'GHI CHÚ'];
    const cellStyles = [
      {bgColor: '8a52f2', textColor: 'ffffff'}, {
        bgColor: '0c6125',
        textColor: 'ffffff',
      }, {bgColor: 'd479d2', textColor: 'ffffff'}];

    cellValues.forEach((value, index) => {
      const cell = guidelinesWorksheet.getCell(
          `${String.fromCharCode(65 + index)}2`);
      cell.value = value;
      cell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: {argb: cellStyles[index].bgColor},
      };
      cell.font = {
        color: {argb: cellStyles[index].textColor},
      };
      cell.alignment = {vertical: 'middle', horizontal: 'center'};
    });

    // Add values to the 3th to 13th row

    const colA = [
      'Problem name',
      'Number of set',
      'Number of individuals',
      'Number of characteristics',
      'Fitness function',
      'Evaluate Function set_1',
      'Evaluate Function set_2',
      'Capacity',
      'Set Many',
      'Set One',
      'Requirements'];

    // Set purple background color and black text for cells B3 to B13
    colA.forEach((value, index) => {
      const cell = guidelinesWorksheet.getCell(`A${3 + index}`);
      cell.value = value;
      cell.fill = {
        type: 'pattern', pattern: 'solid', fgColor: {argb: 'd8bff5'}, // Purple color code
      };
      cell.font = {
        color: {argb: 'FF000000'}, // Black text color code
      };
      cell.alignment = {vertical: 'middle', horizontal: 'left'};

      if (index === 4) {
        const row = guidelinesWorksheet.getRow(7);
        row.height = 400; // Set the desired height
      } else if (index === 10) {
        const row = guidelinesWorksheet.getRow(13);
        row.height = 100; // Set the desired height
      } else if (index === 8) {
        const row = guidelinesWorksheet.getRow(11);
        row.height = 45; // Set the desired height
      } else if (index === 9) {
        const row = guidelinesWorksheet.getRow(12);
        row.height = 45; // Set the desired height
      }
    });

    const colB = [
      'Tên problem được lấy từ dữ liệu người dùng nhập trên trang input',
      'Số lượng các set tham gia được lấy từ dữ liệu người dùng nhập trên trang input',
      'Số lượng tất cả các cá thể ở mỗi set tham gia được lấy từ dữ liệu người dùng nhập trên trang input',
      'Tổng số lượng các thuộc tính của các cá thể tham gia được lấy từ dữ liệu người dùng nhập trên trang input',
      `
    Hàm fitness được lấy từ dữ liệu người dùng nhập trên trang input
    Cách nhập hàm:
    Hiện tại, evaluate function có thể xử lý theo 3 loại biến:
    *$: R: requirement của các individual đối với 1 characteristic cụ thể
    *$: P: properties của các individidual đối với 1 characteristic cụ thể
    *$: W: trọng số của characteristic đối với individual (characteristic đó quan trọng ở mức nào đối với inidividual)
    *$: Ví dụ cụ thể: P1*R1*W1+P2*R2*W2 sẽ là hàm mà phần mềm có thể xử lý được. Phần mềm không xử lý kiểu hàm cũ, trừ khi người dùng để default
     * $: i - index of MatchSet in "matches"
             * $: set - value (1 or 2) represent set 1 (0) or set 2 (1)
             * $: S(set) - Sum of all payoff scores of "set" evaluate by opposite set
             * $: M(i) - Value of specific matchSet's satisfaction eg: M0 (satisfactory of Individual no 0)
             
             * Supported functions:
             * #: SIGMA{S1} calculate sum of all MatchSet of a belonging set eg: SIGMA{S1}
             
             * Supported mathematical calculations:
             * Name:    Usage
             * 1. absolute       : abs(expression)
             * 2. exponent      : (expression)^(expression)
             * 3. sin                 : sin(expression)
             * 4. cos                 : cos(expression)
             * 5. tan                : tan(expression)
             * 6. logarithm     : log(expression)(expression)
             * 7. square root: sqrt(expression)
    `,
      'Hàm đánh giá của set 1 được lấy từ dữ liệu người dùng nhập trên trang input',
      'Hàm đánh giá của set 2 được lấy từ dữ liệu người dùng nhập trên trang input',
      'Người dùng nhập capacity của từng đối tượng (ví dụ: nếu A có thể match với 2 người thì capacity bằng 2)',
      'Set 1 là Set Many do người dùng đã tick trong phần lựa chọn ở trang input',
      'Set 2 là Set One do người dùng đã tick trong phần lựa chọn ở trang input',
      `Người dùng nhập chỉ số yêu cầu của từng cá thể
- Về phần các characteristic của các Individual:
       + Đối với các characteristic dạng chữ, có thể phân tích thành nhiều input khác nhau không có quy luật(ví dụ như skills có thể có cooking, swimming, drawing,...): 
        Các nhóm cần chia thành từng characteristic theo các input đấy (ví dụ như skills thì sẽ tách ra thành cooking, swimming,... và để thành characteristic riêng biệt)
        và đánh giá bằng điểm số (ví dụ swimming: 10 điểm, cooking: 6 điểm).
       +  Đối với các characteristic đánh giá theo mức độ (ví dụ như low, medium, high): 
       Các nhóm cần chuyển đổi thành dạng số theo thang điểm 10 và giới hạn các mức độ theo từng mốc điểm.`];

    // Set purple background color and black text for cells B3 to B13
    colB.forEach((value, index) => {
      const cell = guidelinesWorksheet.getCell(`B${3 + index}`);
      cell.value = value;
      cell.fill = {
        type: 'pattern', pattern: 'solid', fgColor: {argb: 'b4fac7'}, // Green color code
      };
      cell.font = {
        color: {argb: 'FF000000'}, // Black text color code
      };
      cell.alignment = {vertical: 'middle', horizontal: 'left'};

      if (index === 4) {
        cell.alignment = {wrapText: true};
      } else if (index === 10) {
        cell.alignment = {wrapText: true};
      }
    });

    // Set purple background color and black text for cells C3 to C13
    for (let rowNumber = 3; rowNumber <= 13; rowNumber++) {
      const cell = guidelinesWorksheet.getCell(`C${rowNumber}`);
      cell.fill = {
        type: 'pattern', pattern: 'solid', fgColor: {argb: 'fee6ff'}, // Pink color
      };
    }

    guidelinesWorksheet.mergeCells('C11:C12');
    const cellC11 = guidelinesWorksheet.getCell('C11');
    cellC11.value = `Phần này người dùng không cần quá quan tâm vì đây chỉ là note cho backend dễ xử lý hơn`;
    cellC11.alignment = {
      vertical: 'middle', horizontal: 'center', wrapText: true,
    };

    // Add light gray borders to all cells
    guidelinesWorksheet.eachRow((row) => {
      row.eachCell((cell) => {
        cell.border = {
          top: {style: 'thin', color: {argb: 'b0b0b0'}}, // Light gray color
          left: {style: 'thin', color: {argb: 'b0b0b0'}},
          bottom: {style: 'thin', color: {argb: 'b0b0b0'}},
          right: {style: 'thin', color: {argb: 'b0b0b0'}},
        };
      });
    });

    // Save the workbook
    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], {type: 'application/octet-stream'});
    saveAs(blob, 'Input_Matching_Theory.xlsx');
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setExcelFile(event.dataTransfer.files[0]);
    event.target.classList.remove('dragging');
  };

  const handleOnDragEnter = (event) => {
    event.preventDefault();
    event.target.classList.add('dragging');
  };

  const handleOnDragLeave = (event) => {
    event.preventDefault();
    event.target.classList.remove('dragging');
  };

  const handleFileInput = (event) => {
    setExcelFile(event.target.files[0]);
  };

  //Initialize table of individual per set
  const handleColumnsChange = (e) => {
    const value = e.target.value;
    setSetNum(value);
    setColNums(value);
    setSetIndividuals(Array.from({length: value}, () => ''));
    setSetCharacteristics(Array.from({length: value}, () => ''));
    setSetEvaluateFunction(Array.from({length: value}, () => ''));
    setSetMany(Array.from({length: value}, () => ''));
  };
  const generateTable = () => {
    const table = [];
    for (let i = 0; i < 4; i++) {
      const row = [];
      if (i === 0) {
        for (let j = 0; j < colNums; j++) {
          row.push(<th className="th" key={j}>{` Set_${j + 1}`}</th>);
        }
      } else if (i === 1) {
        for (let k = 0; k < colNums; k++) {
          row.push(<td className="td" key={k}>
            <label>
              <input
                  type="checkbox"
                  name={`setType_Set_${k + 1}`}
                  onChange={() => {
                    const updatedSetMany = [...setMany];
                    updatedSetMany[k] = !updatedSetMany[k];
                    setSetMany(updatedSetMany);
                  }}
              />
              <h6>Tick if Set_{k + 1} is Many</h6>
            </label>
          </td>);
        }
      } else if (i === 2) {
        for (let k = 0; k < colNums; k++) {
          row.push(<td className="td" key={k}>
            <input
                type="text"
                className="input-table-data"
                placeholder={`Num individuals Set_${k + 1}`}
                onChange={(e) => {
                  const newSetIndividuals = [...setIndividuals];
                  newSetIndividuals[k] = e.target.value;
                  setSetIndividuals(newSetIndividuals);
                }}
            />
          </td>);
        }
      } else if (i === 3) {
        for (let k = 0; k < colNums; k++) {
          row.push(<td className="td" key={k}>
            <input
                type="text"
                className="input-table-data"
                placeholder={`Evaluate Function Set_${k + 1}`}
                onChange={(e) => {
                  const newSetEvaluateFunction = [...setEvaluateFunction];
                  newSetEvaluateFunction[k] = e.target.value;
                  setSetEvaluateFunction(newSetEvaluateFunction);
                }}
            />
          </td>);
        }
      }
      table.push(<tr key={i}>{row}</tr>);
    }

    return (<table>
      <tbody>{table}</tbody>
    </table>);
  };

  const [showGuideline, setShowGuideline] = useState(false);
  const [showGuidelineText, setShowGuidelineText] = useState(false);
  const handleShowGuideline = () => {
    setShowGuideline(!showGuideline);
    setShowGuidelineText(!showGuidelineText);
  };

  const [isExpanded, setIsExpanded] = useState(false);

  const handleToggle = () => {
    setIsExpanded(!isExpanded);
  };

  return (<>
    <div className="input-page">
      <button className="show-guideline-btn" onClick={handleShowGuideline}>
        {showGuideline ? 'Hide Guideline' : 'Show Guideline'}
      </button>
      {/*TODO: tách component cho phần này nhé, dài quá*/}
      {showGuidelineText && (
          <div className="guideline-text">
            <h5>Step 1: Enter the name of your problem (Text)</h5>
            <h5>
              Step 2: Enter the number of sets{' '}
              <span
                  onClick={handleToggle}
                  className="toggle-icon"
                  style={{
                    cursor: 'pointer',
                    color: isExpanded ? 'gray' : 'gray',
                  }}
              >
                                {isExpanded ? '(▼)' : '(▶)'}
                            </span>
            </h5>
            {isExpanded &&
                (<div className="subsection" id="subsection">
                  <p>
                    The system will display a corresponding
                    table after you fill
                    in the information in Step 2.
                  </p>

                  <p>
                    Determine which set is one/many, then tick
                    the blank box if
                    that set is many. As instructed below:
                  </p>

                  <ul>
                    <li>
                      Set many: Capacity = 1
                      <br/>
                      The number of individuals in the
                      set {'>'} the opponent's
                      set
                    </li>
                    <li>
                      Set one: Capacity {'>'} 1
                      <br/>
                      The number of individuals in the
                      set {'<'} the opponent's
                      set
                    </li>
                  </ul>

                  <p>
                    Fill in the information in the blank box:
                    <ul>
                      <li>
                        "Num individuals of Set_x" - the
                        number of individuals of
                        the corresponding set
                      </li>
                      <li>
                        "Evaluate Function Set_x" - the
                        evaluation function
                        corresponding to that set
                      </li>
                    </ul>
                  </p>
                </div>)
            }

            <h5>Step 3: Enter the number of characteristics of both
              sets</h5>
            <h5>Step 4: Enter the number of total individuals of
              both sets</h5>
            <h5>Step 5: Enter the fitness function which you
              initialize</h5>
            <h5>
              Step 6: Click the button "Get Excel Templates" to
              receive the
              Excel file that contains all the information you
              entered above
            </h5>
            <h5>
              Step 7: Select or drag and drop the Excel file you
              just received
              at the dotted line and the "Choose a file" button
              for the system
              to process your problem
            </h5>
          </div>)
      }

      <Loading isLoading={isLoading}/>
      <p className="header-text">Enter information about your problem</p>

      <div className="input-container">
        <div className="row">
          <Input
              message="Name of the problem"
              type="text"
              error={problemNameError}
              handleOnChange={(e) => setProblemName(e.target.value)}
              value={problemName}
              description="The name should be concise and meaningful, reflecting the nature of the game being analyzed"
              guideSectionIndex={1}
          />
        </div>

        <div className="row">
          <Input
              message="Number of set"
              type="number"
              error={setNumError}
              handleOnChange={handleColumnsChange}
              value={setNum}
              min={SMT.MIN_SET}
              max={SMT.MAX_SET}
              description="A positive number that reflects the number of set involved to ensure that the resulting is valid"
              guideSectionIndex={2}
          />
        </div>
        {setNum ? <div className="table">{generateTable()}</div> : null}

        <div className="row">
          <Input
              message="Number of characteristics"
              text="number"
              error={characteristicsNumError}
              handleOnChange={(e) => setCharacteristicsNum(e.target.value)}
              value={characteristicsNum}
              description="A characteristic is the requirements and the properties that an individuals has that affects their weight during matching"
              guideSectionIndex={3}
              max={20}
          />
          <Input
              message="Number of total individuals"
              text="number"
              error={totalIndividualsNumError}
              handleOnChange={(e) => setTotalIndividualsNum(e.target.value)}
              value={totalIndividualsNum}
              description="A positive number that reflects the number of individuals in each set involved to ensure that the resulting is valid"
              guideSectionIndex={4}
              max={10000}
          />
        </div>

        <div className="row">
          <Input
              message="Fitness function"
              type="text"
              error={fitnessFunctionError}
              handleOnChange={(e) => setFitnessFunction(e.target.value)}
              value={fitnessFunction}
              description="The fitness function is a mathematical function that represents the payoff that a player receives for a specific combination of strategies played by all the players in the game"
              guideSectionIndex={5}
              // iconStyle={{fontSize: '1.2em', verticalAlign: 'center'}}
          />
        </div>
        <div className="btn" onClick={handleGetExcelTemplate}>
          <p>Get Excel Template</p>
          <img src={ExcelImage} alt=""/>
        </div>
      </div>
      <div className="guide-box">
        <p>
          Get the Excel file template, input your data, then drag & drop it to
          the box below
        </p>
        <Link
            to="/guide"
            className="guide-link"
            onClick={(e) => setGuideSectionIndex(9)}
        >
          {' '}
          Learn more on how to input to file Excel
        </Link>
      </div>
      {excelFileError && <p className="file-error">{excelFileError}</p>}
      <div
          className={excelFileError ? 'drag-area file-error' : 'drag-area'}
          onDrop={handleDrop}
          onDragEnter={handleOnDragEnter}
          onDragLeave={handleOnDragLeave}
          onDragOver={handleOnDragEnter}
      >
        <p className="drag-text">
          {excelFile ? excelFile.name : 'Drag and drop a file here'}
        </p>
        <label htmlFor="select-file" id="select-file-label">
          Choose a file
        </label>
        <input type="file" id="select-file" onChange={handleFileInput}/>
      </div>
    </div>
  </>);
}


