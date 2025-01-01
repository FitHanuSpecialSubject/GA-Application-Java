import * as XLSX from '@e965/xlsx';
import {MATCHING} from '../const/excel_const';

/**
 * Tạo một sheet từ thông tin cấu hình máy tính.
 * @param {Object} appData - Dữ liệu ứng dụng chứa thông tin cấu hình.
 * @returns {Object} - Sheet được tạo.
 */
export const createSystemInfoSheet = (appData) => {
  // Kiểm tra nếu appData hoặc computerSpecs không tồn tại
  const computerSpecs = appData?.result?.data?.computerSpecs || {};

  return XLSX.utils.aoa_to_sheet([
    ["Operating System Family", computerSpecs.osFamily || "unknown"],
    ["Operating System Manufacturer", computerSpecs.osManufacturer || "unknown"],
    ["Operating System Version", computerSpecs.osVersion || "unknown"],
    ["CPU Name", computerSpecs.cpuName || "unknown"],
    ["CPU Physical Cores", computerSpecs.cpuPhysicalCores || "unknown"],
    ["CPU Logical Cores", computerSpecs.cpuLogicalCores || "unknown"],
    ["Total Memory", computerSpecs.totalMemory || "unknown"],
  ]);
};

/**
 * Tạo một sheet từ các thông số cấu hình.
 * @param {Object} appData - Dữ liệu ứng dụng chứa thông số cấu hình.
 * @returns {Object} - Sheet 2 chứa các thông số.
 */
export const createParameterConfigSheet = (appData) => {
  const numberOfCores = appData.result.params.distributedCoreParam === 'all' ? 'All available cores' : appData.result.params.distributedCoreParam + " cores";

  return XLSX.utils.aoa_to_sheet([
    ["Number of distributed cores", numberOfCores],
    ["Population size", appData.result.params.populationSizeParam],
    ["Number of crossover generation", appData.result.params.generationParam],
    ["Optimization execution max time (in milliseconds)", appData.result.params.maxTimeParam],
  ]);
};

/**
 * Tải dữ liệu bài toán song song từ workbook
 * @param {Object} workbook - Workbook Excel chứa dữ liệu bài toán
 * @param {number} sheetNumber - Số thứ tự sheet cần đọc
 * @returns {Object} - Dữ liệu bài toán
 */
export const loadProblemDataParallel = async (workbook, sheetNumber) => {

  const sheetName = workbook.SheetNames[sheetNumber];
  const sheet = workbook.Sheets[sheetName];
  const problemName = getCellValueStr(sheet, 'B1');
  const setNum = getCellValueNum(sheet, 'B2');
  const totalNumberOfIndividuals = getCellValueNum(sheet, 'B3');
  const characteristicNum = getCellValueNum(sheet, 'B4');
  const fitnessFunction = getCellValueStr(sheet, 'B5');

  let currentRow = 6 + setNum;
  let characteristics = [];

  let currentColumnIndex = XLSX.utils.decode_col(MATCHING.CHARACTERISTIC_START_COL);

  // Đọc các đặc tính từ bảng
  for (let i = currentColumnIndex; ; i++) {
    const cellAddress = XLSX.utils.encode_cell({ c: i, r: currentRow - 1 });
    const cell = sheet[cellAddress];
    // Break if cell is empty or undefined
    if (!cell || !cell.v) {
      break;
    }
    characteristics.push(cell.v);
  }

  // Đọc các bộ dữ liệu (sets)
  const individuals = [];
  let setEvaluateFunction = [];
  let individualSetIndices = [];
  let individualNames = [];
  let individualProperties = [];
  let individualRequirements = [];
  let individualWeights = [];
  let individualCapacities = [];

  let setNames = [];
  let setTypes = [];
  let individualNum = null;
  let setType = null;
  let setName = null;

  // Load evaluate functions for each set
  for (let j = 0; j < setNum; j++) {
    const evaluateFunction = getCellValueStr(sheet, `B${6 + j}`)
    setEvaluateFunction.push(evaluateFunction);
  }

  for (let g = 0; g < setNum; g++) {
    setName = sheet[`A${currentRow}`]?.v || '';
    setType = sheet[`B${currentRow}`]?.v || '';
    setNames.push(setName);
    setTypes.push(setType);

    individualNum = sheet[`D${currentRow}`]?.v || 0;

    for (let i = 0; i < individualNum; i++) {
      let name = sheet[`A${currentRow + 1}`]?.v;
      if (Object.is(name, undefined) || Object.is(name, null) || Object.is(name, '')) {
        name = `no_name_${i + 1}`;
      }
      const properties = [];
      const requirements = [];
      const weights = [];

      for (let k = 0; k < characteristicNum; k++) {
        requirements.push(
            sheet[XLSX.utils.encode_cell({c: k + 4, r: currentRow})]?.v
            || 0);
        weights.push(
            sheet[XLSX.utils.encode_cell({c: k + 4, r: currentRow + 1})]?.v
            || 0);
        properties.push(
            sheet[XLSX.utils.encode_cell({c: k + 4, r: currentRow + 2})]?.v
            || 0);

      }

      individualNames.push(name);
      individualSetIndices.push(g);
      individualProperties.push(properties);
      individualRequirements.push(requirements);
      individualWeights.push(weights);

      // Load capacity
      const capacityValue = await sheet[`C${currentRow + 1}`]?.v;
      if (capacityValue !== undefined && capacityValue !== null) {
        individualCapacities.push(capacityValue);
      }

      currentRow += 3;
    }

    currentRow += 1;
  }

  return {
    problemName,
    characteristicNum,
    setNum,
    setNames,
    setTypes,
    totalNumberOfIndividuals,
    individualNames,
    characteristics,
    individualSetIndices,
    individualCapacities,
    individualRequirements,
    individualProperties,
    individualWeights,
    individuals,
    fitnessFunction,
    setEvaluateFunction,
  };
};

/**
 * Tải dữ liệu Exclude Pairs từ workbook
 * @param {Object} workbook - Workbook Excel chứa dữ liệu bài toán
 * @param {number} sheetNumber - Số thứ tự sheet cần đọc
 * @returns {Object} -  Exclude Pairs
 */
export const loadExcludePairs = async (workbook, sheetNumber) => {
  const sheetName = workbook.SheetNames[sheetNumber];
  const result = {};
  if (sheetName !== "Exclude Pairs") return result;
  const sheet = workbook.Sheets[sheetName];
  let index = 2;
  while (getCellValueStr(sheet, ("A" + index)) !== "" && getCellValueStr(sheet, ("B" + index)) !== "") {
    const individual = getCellValueNum(sheet, ("A" + index));
    const excludedFrom = getCellValueStr(sheet, ("B" + index)).split(",").map(e => parseInt(e));
    result[individual] = excludedFrom;
    index++;
  }
  return result;
}

/**
 * @deprecated
 * @param workbook
 * @param sheetNumber
 * @returns {Promise<{totalNumberOfIndividuals: *, characteristics: *[], setNum: *, fitnessFunction: *, problemName: *, individuals: *[], characteristicNum: *, setEvaluateFunction: *[]}>}
 */
export const loadProblemDataOld = async (workbook, sheetNumber) => {
  const sheetName = workbook.SheetNames[sheetNumber];
  const sheet = workbook.Sheets[sheetName];

  // Đọc các giá trị trong Excel
  const problemName = sheet['B1']?.v || '';
  const setNum = sheet['B2']?.v || 0;
  const totalNumberOfIndividuals = sheet['B3']?.v || 0;
  const characteristicNum = sheet['B4']?.v || 0;
  const fitnessFunction = sheet['B5']?.v || '';

  let currentRow = 6 + Number(setNum);
  // let currentIndividual = 0;
  let characteristics = [];
  let errorMessage = '';

  // LOAD CHARACTERISTICS
  for (let i1 = 4; i1 < characteristicNum + 4; i1++) {
    const characteristicName = await sheet[
        XLSX.utils.encode_cell({
          c: i1,
          r: currentRow - 1,
        })
        ];

    if (characteristicName) {
      characteristics.push(characteristicName['v']);
    }
  }

  // LOAD SET
  const individuals = [];
  let setEvaluateFunction = [];
  // const row = characteristicNum;
  // const col = 3;
  let individualNum = null;
  // let argumentCell = null;
  let individualName = null;
  let setType = null;
  let capacity = null;
  let setName = null;

  // Add evaluate function
  for (let j = 0; j < setNum; j++) {
    // let evaluateFunction = await sheet[`B${6 + j}`]['v'];
    let evaluateFunction = getCellValueStr(sheet, `B${6 + j}`)
    setEvaluateFunction.push(evaluateFunction);
  }

  // Load individuals
  for (let g = 0; g < setNum; g++) {
    setName = sheet[`A${currentRow}`]?.v || '';
    setType = sheet[`B${currentRow}`]?.v || '';

    if (g === 0) {
      setType = 0;
    } else if (g === 1) {
      setType = 1;
    }

    individualNum = sheet[`D${currentRow}`]?.v || 0;

    if (typeof individualNum !== 'number') {
      errorMessage = `Error when loading Set_${g + 1}, row = ${currentRow}. Number of individual is invalid`;
      throw new Error(errorMessage);
    }

    for (let i = 0; i < individualNum; i++) {
      individualName = sheet[`A${currentRow + 1}`]?.v || '';
      capacity = sheet[`C${currentRow + 1}`]?.v || 0;

      let argument = [];
      for (let k = 0; k < characteristicNum; k++) {
        argument[k] = [];
        for (let l = 0; l < 3; l++) {
          const argumentCell = sheet[XLSX.utils.encode_cell({ c: k + 4, r: currentRow + l })];
          if (argumentCell === undefined) {
            errorMessage = `Error when loading Individual_${i + 1}, row = ${currentRow}, column = ${k + 1}. Characteristic_ of strategy are invalid`;
            throw new Error(errorMessage);
          }
          argument[k][l] = argumentCell['v'];
        }
      }

      individuals.push({ set: setName, setType, individualName, capacity, argument });
      currentRow += 3;
    }
    currentRow += 1;
  }

  return {
    problemName,
    characteristicNum,
    setNum,
    totalNumberOfIndividuals,
    characteristics,
    individuals,
    fitnessFunction,
    setEvaluateFunction,
  };
};

/**
 * get cell value as String
 *
 * @param sheet
 * @param address
 * @returns {string} empty String if error
 */
const getCellValueStr = (sheet, address) => {
  try {
    return sheet[address]?.v?.toString() || "";
  } catch (error) {
    console.error('Error parsing string cell value, address: '
        + address + ' , details: ' + error);
    return "";
  }
}

/**
 * get cell value as Number
 *
 * @param sheet
 * @param address
 * @returns
 *
 * @throws error if error
 */
export const getCellValueNum = (sheet, address) => {
    let val = Number(sheet[address]?.v);
    if (Number.isNaN(val)) {
      throw TypeError('Invalid number format, cell address: ' + address);
    }
    return val;
}

/**
 * 
 * @param {Map<string, number>} fitnessValues
 * @param {Map<string, number>} runtimes
 * @param {Map<string, string>} computerSpecs
 * @param {Map<string, string>} params
 * @returns {Blob} Excel file
 */
export async function exportInsights(fitnessValues, runtimes, computerSpecs, params) {
    const workbook = XLSX.utils.book_new();

    const algorithms = Object.keys(fitnessValues);

    const sheet1 = XLSX.utils.aoa_to_sheet([
        ['Iteration',...algorithms]
    ]);

    const totalRun = fitnessValues[algorithms[0]].length;
    for (let i = 0; i < totalRun; i++) {
        const values = Object.values(fitnessValues)
            .map((values) => values[i]);
        const row = [i + 1 , ...values];

        XLSX.utils.sheet_add_aoa(sheet1, [row], { origin: -1 });
    }

    // write runtime values to the second sheet
    const sheet2 = XLSX.utils.aoa_to_sheet([['Iteration', ...algorithms]]);
    for (let i = 0; i < totalRun; i++) {
        const values = Object
            .values(runtimes)
            .map((value) => value[i]);
        const row = [i + 1 , ...values];

        XLSX.utils.sheet_add_aoa(sheet2, [row], { origin: -1 });
    }

    // write parameter configurations to the third sheet
    const numberOfCores = params.distributedCoreParam === 'all'
        ? 'All available cores'
        : params.distributedCoreParam + ' cores';
    const sheet3 = XLSX.utils.aoa_to_sheet([
      ['Number of distributed cores', numberOfCores],
      ['Population size', params.populationSizeParam],
      [
        'Number of crossover generation',
        params.generationParam],
      [
        'Optimization execution max time (milliseconds)',
        params.maxTimeParam],
    ]);

    // write computer specifications to the fourth sheet
    const sheet4 = XLSX.utils.aoa_to_sheet([
      [
        'Operating System Family',
        computerSpecs?.osFamily || 'unknown'],
      [
        'Operating System Manufacturer',
        computerSpecs?.osManufacturer || 'unknown'],
      [
        'Operating System Version',
        computerSpecs?.osVersion || 'unknown'],
      ['CPU Name', computerSpecs?.cpuName || 'unknown'],
      [
        'CPU Physical Cores',
        computerSpecs?.cpuPhysicalCores || 'unknown'],
      [
        'CPU Logical Cores',
        computerSpecs?.cpuLogicalCores || 'unknown'],
      [
        'Total Memory',
        computerSpecs?.totalMemory || 'unknown'],
    ]);

    // add sheets to the workbook
    XLSX.utils.book_append_sheet(workbook, sheet1, 'Fitness Values');
    XLSX.utils.book_append_sheet(workbook, sheet2, 'Runtimes');
    XLSX.utils.book_append_sheet(workbook, sheet3, 'Parameter Configurations');
    XLSX.utils.book_append_sheet(workbook, sheet4, 'Computer Specifications');

    // save the workbook
    const wbout = await XLSX.write(workbook, {bookType: 'xlsx', type: 'array'});
    return new Blob([wbout], {type: 'application/octet-stream'});
}
