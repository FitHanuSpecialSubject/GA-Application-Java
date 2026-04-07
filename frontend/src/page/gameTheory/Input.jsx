import React from "react";
import { useState, useEffect } from "react";
import "../../module/gameTheory/css/input.scss";
import SpecialPlayerInput from "../../module/gameTheory/component/specialPlayerInput";
import Input from "../../module/core/component/input";
import { saveAs } from "file-saver";
import { useContext } from "react";
import DataContext from "../../module/core/context/DataContext";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import Loading from "../../module/core/component/Loading";
import MaxMinCheckbox from "../../module/core/component/MaxMinCheckbox";
import PopupContext from "../../module/core/context/PopupContext";
import { validateExcelFile } from "../../utils/file_utils";
import ExcelJS from "exceljs";
import {
  loadConflictSet,
  loadNormalPlayers,
  loadProblemInfoGT,
  loadSpecialPlayer,
} from "../../utils/excel_utils";
import { GAME_THEORY_WORKBOOK } from "../../const/excel_const";
import GT_GUIDELINE from "../../module/core/asset/workbook/gtguidelines.xlsx";
import { FaRegFileExcel } from "react-icons/fa6";

export default function InputPage() {
  // initialize form data
  const [excelFile, setExcelFile] = useState(null);

  const [problemName, setProblemName] = useState("");
  const [specialPlayerExists, setSpecialPlayerExists] = useState("");
  const [specialPlayerPropsNum, setSpecialPlayerPropsNum] = useState(null);
  const [normalPlayerNum, setNormalPlayerNum] = useState("");
  const [normalPlayerPropsNum, setNormalPlayerPropsNum] = useState("");
  const [fitnessFunction, setFitnessFunction] = useState("DEFAULT");
  const [playerPayoffFunction, setPlayerPayoffFunction] = useState("DEFAULT");
  const [isMaximizing, setIsMaximizing] = useState(false);
  const [defaultStrategy, setDefaultStrategy] = useState("");
  const [problemType, setProblemType] = useState("");
  const [problemNameError, setProblemNameError] = useState("");
  const [specialPlayerPropsNumError, setSpecialPlayerPropsNumError] =
    useState("");
  const [normalPlayerNumError, setNormalPlayerNumError] = useState("");
  const [normalPlayerPropsNumError, setNormalPlayerPropsNumError] =
    useState("");
  /** Nếu function và payoff để trống thì thành value mặc định rồi nên chỗ này không cần
  check ở hiện tại, sau này có thêm function khác thì check sau **/
  const [fitnessFunctionError, setFitnessFunctionError] = useState("");
  const [playerPayoffFunctionError, setPlayerPayoffFunctionError] =
    useState("");
  const [isLoading, setIsLoading] = useState(false);

  const [excelFileError, setExcelFileError] = useState("");

  const { setAppData, setGuideSectionIndex, setFavicon } =
    useContext(DataContext);
  const { displayPopup } = useContext(PopupContext);

  const navigate = useNavigate();
  // check if the uploaded file is an excel file
  useEffect(() => {
    if (excelFile) {
      try {
        if (validateExcelFile(excelFile)) {
          readExcelFile(excelFile);
        } else {
          displayPopup(
            "Something went wrong!",
            "The file was not an Excel file!",
            true,
          );
          setExcelFileError("The file was not an Excel file!");
        }
      } catch (error) {
        console.error(error);
        setExcelFile(null);
        displayPopup("Error", error.message, true);
      }
    }
  }, [excelFile]);
  useEffect(() => {
    setFavicon("idle");
  }, []);
  // read file
  const readExcelFile = async (file) => {
    const reader = new FileReader();
    setIsLoading(true);

    try {
      reader.readAsArrayBuffer(file);
      reader.onload = async () => {
        const data = reader.result;
        const workbook = await new ExcelJS.Workbook().xlsx.load(data);

        const problemSheet = workbook.getWorksheet(
          GAME_THEORY_WORKBOOK.PROBLEM_INFO_SHEET_NAME,
        );
        if (!problemSheet) {
          displayPopup(
            "Excel Error",
            `The sheet '${GAME_THEORY_WORKBOOK.PROBLEM_INFO_SHEET_NAME}' is missing. Please check the file.`,
            true,
          );
          setExcelFile(null);
          setIsLoading(false);
          return;
        }

        let problemInfo;
        try {
          problemInfo = await loadProblemInfoGT(workbook);
        } catch (e) {
          console.error(e);
          setIsLoading(false);
          setExcelFile(null);
          displayPopup("Something went wrong!", e.message, true);
          return;
        }

        if (!problemInfo) return;

        let specialPlayers = null;
        let players = null;
        let conflictSet = null;

        const specialPlayerRequired = problemSheet.getCell("B2").value === 1;
        if (specialPlayerRequired) {
          if (
            !workbook.getWorksheet(
              GAME_THEORY_WORKBOOK.SPECIAL_PLAYER_SHEET_NAME,
            )
          ) {
            displayPopup(
              "Excel Error",
              `The sheet '${GAME_THEORY_WORKBOOK.SPECIAL_PLAYER_SHEET_NAME}' is missing. Please check the file.`,
              true,
            );
            setExcelFile(null);
            setIsLoading(false);
            return;
          }
          try {
            specialPlayers = await loadSpecialPlayer(
              workbook,
              specialPlayerPropsNum,
            );
          } catch (e) {
            console.error(e);
            setExcelFile(null);
            setIsLoading(false);
            displayPopup(
              "Something went wrong!",
              `Looks like your file doesn't have a '${GAME_THEORY_WORKBOOK.SPECIAL_PLAYER_SHEET_NAME}' sheet`,
              true,
            );
            return;
          }
          if (!specialPlayers) return;
        }

        if (
          !workbook.getWorksheet(GAME_THEORY_WORKBOOK.NORMAL_PLAYER_SHEET_NAME)
        ) {
          displayPopup(
            "Excel Error",
            `The sheet '${GAME_THEORY_WORKBOOK.NORMAL_PLAYER_SHEET_NAME}' is missing. Please check the file.`,
            true,
          );
          setExcelFile(null);
          setIsLoading(false);
          return;
        }
        try {
          players = await loadNormalPlayers(
            workbook,
            problemInfo.normalPlayerNum,
            problemInfo.normalPlayerPropsNum,
          );
        } catch (error) {
          console.error(error);
          setIsLoading(false);
          setExcelFile(null);
          displayPopup("Something went wrong!", error.message, true);
          return;
        }
        if (!players) return;

        if (
          !workbook.getWorksheet(
            GAME_THEORY_WORKBOOK.CONFLICT_MATRIX_SHEET_NAME,
          )
        ) {
          displayPopup(
            "Excel Error",
            `The sheet '${GAME_THEORY_WORKBOOK.CONFLICT_MATRIX_SHEET_NAME}' is missing. Please check the file.`,
            true,
          );
          setExcelFile(null);
          setIsLoading(false);
          return;
        }
        try {
          conflictSet = await loadConflictSet(workbook);
        } catch (error) {
          console.error(error);
          setExcelFile(null);
          setIsLoading(false);
          displayPopup(
            "Something went wrong!",
            `Looks like your file doesn't have a '${GAME_THEORY_WORKBOOK.CONFLICT_MATRIX_SHEET_NAME}' sheet`,
            true,
          );
          return;
        }
        if (!conflictSet) return;
        const experimentConfig = { 
          timestamp: new Date().toISOString(),
          parameters: {
            specialPlayerExists: problemInfo.specialPlayerExists,
            specialPlayerPropsNum: problemInfo.specialPlayerPropsNum,
            normalPlayerNum: problemInfo.normalPlayerNum,
            normalPlayerPropsNum: problemInfo.normalPlayerPropsNum,
            fitnessFunction: problemInfo.fitnessFunction,
            playerPayoffFunction: problemInfo.playerPayoffFunction,
            isMaximizing: problemInfo.isMaximizing
          },
          results: null  
        };  
        localStorage.setItem( 
          "experimentConfig",
          JSON.stringify(experimentConfig)
        );
        //duc


        setAppData({
          problem: {
            name: problemInfo.problemName,
            specialPlayerExists: problemInfo.specialPlayerExists,
            specialPlayerPropsNum: problemInfo.specialPlayerPropsNum,
            normalPlayerNum: problemInfo.normalPlayerNum,
            normalPlayerPropsNum: problemInfo.normalPlayerPropsNum,
            fitnessFunction: problemInfo.fitnessFunction,
            playerPayoffFunction: problemInfo.playerPayoffFunction,
            isMaximizing: problemInfo.isMaximizing,
            specialPlayer: specialPlayers,
            players: players,
            conflictSet: conflictSet,
          },
        });

        setIsLoading(false);
        navigate("/input-processing");
      };
    } catch (error) {
      console.error(error);
      setIsLoading(false);
      setExcelFile(null);
      displayPopup(
        "Something went wrong!",
        "Check the input file again or contact the admin!",
        true,
      );
    }
  };

  useEffect(() => {
    if (problemType) {
      displayPopup("Invalid Form!", problemType, true);
      setProblemType("");
    }
  }, [problemType]);
  const handleGetExcelTemplate = () => {
    if (validateForm()) {
      downloadExcel().then();
    }
    // else {
    //   displayPopup(
    //     "Invalid Form!",
    //     "Make sure you have filled all the required fields.",
    //     true,
    //   );
    // }
  };

  // Potential bug: the error message only shows after the first time the user clicks the button
  const validateForm = () => {
    let error = false;
    let msg = "";
    const validFunctionPattern = /^[a-zA-Z0-9s+\-*/^()]+$/;

    // check if the problem name is empty
    if (problemName.length === 0 || problemName.length > 255) {
      setProblemNameError(
        "Problem name must not be empty or exceed 255 characters",
      );
      msg = "Problem name must not be empty or exceed 255 characters";
      error = true;
    } else {
      setProblemNameError("");
    }

    if (specialPlayerExists) {
      // if special player exists, then the number of special players must not be empty
      if (!specialPlayerPropsNum) {
        setSpecialPlayerPropsNumError(
          "Special player properties must not be empty",
        );
        error = true;
      } else {
        setSpecialPlayerPropsNumError("");
      }
    }
    // check if the number of normal players is empty
    // check if the number of normal players is in range [2, 1000], any future update will require to update this range
    if (!normalPlayerNum) {
      setNormalPlayerNumError("Normal player number must not be empty");
      msg = "Normal player number must not be empty";
      error = true;
    } else if (
      parseInt(normalPlayerNum) < 2 ||
      parseInt(normalPlayerNum) > 1000
    ) {
      setNormalPlayerNumError(
        "Normal player number must be between 2 and 1000",
      );
      msg = "Normal player number must be between 2 and 1000";
      error = true;
    } else {
      setNormalPlayerNumError("");
    }

    // check if the number of normal player properties is empty
    /** check if the number of normal players' characteristics is in
    range [1, 20], any future update will require to update this range **/
    if (!normalPlayerPropsNum) {
      setNormalPlayerPropsNumError(
        "Normal player properties must not be empty",
      );
      msg = "Normal player properties must not be empty";
      error = true;
    } else if (
      parseInt(normalPlayerPropsNum) < 1 ||
      parseInt(normalPlayerPropsNum) > 20
    ) {
      setNormalPlayerPropsNumError(
        "Normal player properties must be between 1 and 20",
      );
      msg = "Normal player properties must be between 1 and 20";
      error = true;
    } else {
      setNormalPlayerPropsNumError("");
    }

    // check if the number of strategies is empty
    // if (!fitnessFunction) {
    //   setFitnessFunctionError("Fitness function must not be empty");
    //   msg = "Fitness function must not be empty";
    //   error = true;
    // } else {
    //   setFitnessFunctionError("");
    // }
    // check if the number of strategies is empty
    // if (!playerPayoffFunction) {
    //   setPlayerPayoffFunctionError("Player payoff function must not be empty");
    //   msg = "Player payoff function must not be empty";
    //   error = true;
    // } else {
    //   setPlayerPayoffFunctionError("");
    // }

    // function phải viết liền dấu (không có khoảng trắng, vd: p1-p2)
    if (fitnessFunction.length === 0) {
      setFitnessFunction("DEFAULT");
    } else {
      if (!validFunctionPattern.test(fitnessFunction)) {
        setFitnessFunctionError("Fitness function is invalid");
        msg = "Fitness function is invalid";
        error = true;
      }
    }

    // function phải viết liền dấu (không có khoảng trắng, vd: p1-p2)
    if (playerPayoffFunction.length === 0) {
      setPlayerPayoffFunction("DEFAULT");
    } else {
      if (!validFunctionPattern.test(playerPayoffFunction)) {
        setPlayerPayoffFunctionError("Player payoff function is invalid");
        msg = "Player payoff function is invalid";
        error = true;
      }
    }

    setProblemType(msg);

    // if there is no error, return true
    return !error;
  };

  // tao file excel dua tren input
  const downloadExcel = async () => {
    const workbook = new ExcelJS.Workbook();
    const payoffFunction = playerPayoffFunction;

    // write problem information to sheet1
    const sheet1 = workbook.addWorksheet(
      GAME_THEORY_WORKBOOK.PROBLEM_INFO_SHEET_NAME,
    );
    sheet1.addRows([
      ["Problem name", problemName],
      ["Special Player exists (0 - No, 1 -Yes) ", specialPlayerExists ? 1 : 0],
      ["Number of properties of special player", Number(specialPlayerPropsNum)],
      ["Number of normal players", Number(normalPlayerNum)],
      [
        "Number of properties of each normal player",
        Number(normalPlayerPropsNum),
      ],
      ["Fitness function", fitnessFunction],
      ["Player payoff function", payoffFunction],
    ]);

    let isMaximizingRow = ["Is maximzing problem", "False"];
    if (isMaximizing) {
      isMaximizingRow = ["Is maximzing problem", "True"];
    }
    sheet1.addRow(isMaximizingRow);
    if (specialPlayerExists) {
      const sheet2 = workbook.addWorksheet(
        GAME_THEORY_WORKBOOK.SPECIAL_PLAYER_SHEET_NAME,
      );
      sheet2.addRow(["Properties", "Weights"]);
    }
    const sheet3 = workbook.addWorksheet(
      GAME_THEORY_WORKBOOK.NORMAL_PLAYER_SHEET_NAME,
    );
    const noS = Number(defaultStrategy) <= 0 ? 1 : Number(defaultStrategy);
    let curRow = 1;
    for (let p = 0; p < normalPlayerNum; p++) {
      sheet3.getCell(curRow, 1).value = "Player " + (p + 1);
      sheet3.getCell(curRow, 2).value = noS;
      for (let s = 0; s < noS; s++) {
        sheet3.getCell(curRow + s + 1, 1).value = "Strategy " + (s + 1);
        for (let p = 0; p < normalPlayerPropsNum; p++) {
          sheet3.getCell(curRow + s + 1, 1 + p + 1).value =
            "Property " + (p + 1);
        }
      }
      curRow += noS + 1;
    }

    // Write the sheet4(blank sheet) for user to input conflict matrix
    workbook.addWorksheet(GAME_THEORY_WORKBOOK.CONFLICT_MATRIX_SHEET_NAME);

    try {
      const guidelinesWorkbook = new ExcelJS.Workbook();
      const response = await fetch(GT_GUIDELINE);
      const arrayBuffer = await response.arrayBuffer();
      await guidelinesWorkbook.xlsx.load(arrayBuffer);
      const guidelinesSheet = workbook.addWorksheet(
        GAME_THEORY_WORKBOOK.GUIDELINE_SHEET_NAME,
      );
      guidelinesSheet.model = guidelinesWorkbook.getWorksheet(
        GAME_THEORY_WORKBOOK.GUIDELINE_SHEET_NAME,
      ).model;
    } catch (error) {
      console.error(error);
      setExcelFile(null);
      displayPopup(
        "Failed to load guidelines sheet. Please check the file and try again.",
        true,
      );
    }

    // Save workbooks
    const wbout = await workbook.xlsx.writeBuffer();
    const blob = new Blob([wbout]);
    saveAs(blob, `${problemName}_game_theory.xlsx`);
  };

  const handleDrop = (event) => {
    event.preventDefault();
    setExcelFile(event.dataTransfer.files[0]);
    event.target.classList.remove("dragging");
  };

  const handleOnDragEnter = (event) => {
    event.preventDefault();
    event.target.classList.add("dragging");
  };

  const handleOnDragLeave = (event) => {
    event.preventDefault();
    event.target.classList.remove("dragging");
  };

  const handleFileInput = (event) => {
    if (event.target.value == null) return;
    setExcelFile(event.target.files[0]);
    event.target.value = null;
  };

  return (
    <>
      <div className="input-page">
        <Loading isLoading={isLoading} />
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
            <SpecialPlayerInput
              specialPlayerExists={specialPlayerExists}
              setSpecialPlayerExists={setSpecialPlayerExists}
              specialPlayerPropsNum={specialPlayerPropsNum}
              setSpecialPlayerPropsNum={setSpecialPlayerPropsNum}
              error={specialPlayerPropsNumError}
            />
          </div>

          <div className="row">
            <Input
              type="number"
              message="Number of normal players"
              text="number"
              error={normalPlayerNumError}
              handleOnChange={(e) => setNormalPlayerNum(e.target.value)}
              value={normalPlayerNum}
              description="A positive number that reflects the number of players involved
              to ensure that the resulting Nash equilibrium is valid"
              guideSectionIndex={4}
            />
            <Input
              message="Number of properties each strategy of normal player"
              text="number"
              error={normalPlayerPropsNumError}
              handleOnChange={(e) => setNormalPlayerPropsNum(e.target.value)}
              value={normalPlayerPropsNum}
              description="A property is a characteristic or attribute that a player
              has that affects their actions or outcomes in the game"
              guideSectionIndex={5}
            />
          </div>

          <div className="row">
            <Input
              message="Default number of strategies"
              type="number"
              error={playerPayoffFunctionError}
              handleOnChange={(e) => setDefaultStrategy(e.target.value)}
              value={defaultStrategy}
              description={
                "This value is only for generating your Excel template. You should use the number of strategies " +
                "that most of the players have, then manually edit any further exception. Leave it blank in " +
                "case you want an example dataset."
              }
              guideSectionIndex={7}
            />
          </div>

          <div className="row">
            <Input
              message="Fitness function"
              type="text"
              error={fitnessFunctionError}
              handleOnChange={(e) => setFitnessFunction(e.target.value)}
              value={fitnessFunction}
              description="The fitness function is a mathematical function that
                represents the payoff that a player receives for a specific
                combination of strategies played by all the players in the game"
              guideSectionIndex={6}
            />
          </div>

          <div className="row">
            <Input
              message="Player payoff function"
              type="text"
              error={playerPayoffFunctionError}
              handleOnChange={(e) => setPlayerPayoffFunction(e.target.value)}
              value={playerPayoffFunction}
              description="The player payoff function is a mathematical function that determines
              the outcome of the game by assigning a payoff value to each player based on the
              strategies chosen by all the players in the game"
              guideSectionIndex={7}
            />
          </div>

          <div className="row">
            <MaxMinCheckbox
              isMaximizing={isMaximizing}
              setIsMaximizing={setIsMaximizing}
            />
          </div>
        </div>
        <div
          className="btn btn-success d-flex justify-content-center border-1 p-3"
          onClick={handleGetExcelTemplate}
        >
          <FaRegFileExcel className="me-0 fs-4" />
          Get Excel Template
        </div>

        <div className="guide-box">
          <p>
            Get the Excel file template, input your data, then drag & drop it to
            the box below
          </p>
          <Link
            to="/guide"
            className="guide-link"
            onClick={() => setGuideSectionIndex(9)}
          >
            {" "}
            Learn more on how to input to file Excel
          </Link>
        </div>

        {excelFileError && <p className="file-error">{excelFileError}</p>}
        <div
          className={excelFileError ? "drag-area file-error" : "drag-area"}
          onDrop={handleDrop}
          onDragEnter={handleOnDragEnter}
          onDragLeave={handleOnDragLeave}
          onDragOver={handleOnDragEnter}
        >
          <p className="drag-text">
            {excelFile ? excelFile.name : "Drag and drop a file here"}
          </p>
          <label htmlFor="select-file" id="select-file-label">
            Choose a file
          </label>
          <input
            accept=".xlsx"
            type="file"
            id="select-file"
            onChange={handleFileInput}
          />
        </div>
      </div>
    </>
  );
}
