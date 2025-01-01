import Navbar from "../module/core/component/Navbar";
import InputPage from "../page/gameTheory/Input";
import "../module/core/css/index.scss";
import { Routes, Route } from 'react-router-dom'
import { useState } from "react";
import DataContext from "../module/core/context/DataContext";
import InsightPage from "../page/Insight";
import PopupContext from "../module/core/context/PopupContext";
import Popup from '../module/core/component/Popup'
import stableMatchingRouter from "./stableMatching";
import gameTheoryRouter from "./gameTheory";

function App() {
  const [appData, setAppData] = useState(null)
  const [guideSectionIndex, setGuideSectionIndex] = useState(0)
  const [popupError, setPopupError] = useState(false)
  const [popupTitle, setPopupTitle] = useState("")
  const [popupMessage, setPopupMessage] = useState("")
  const [showPopup, setShowPopup] = useState(false)
  const [popupOkCallBack] = useState()
  const displayPopup = (title, message, error) => {
    setShowPopup(true)
    setPopupTitle(title)
    setPopupMessage(message)
    if (error) {
      setPopupError(error)
    }
  }
  if(appData === null && guideSectionIndex === 0) {
    console.log("Backend URL:");
    console.log(`${process.env.REACT_APP_BACKEND_URL}:${process.env.REACT_APP_BACKEND_PORT}`);
  }
  const StableMatchingRouter = stableMatchingRouter();
  const GameTheoryRouter = gameTheoryRouter();
  return (
    <DataContext.Provider value={{ appData, setAppData, guideSectionIndex, setGuideSectionIndex }}>
      <PopupContext.Provider value={{ displayPopup }}>
        <div className="App">
          <Navbar/>
          <Routes>
            { StableMatchingRouter }
            { GameTheoryRouter }
            <Route path='/insights' element={<InsightPage />} />
            <Route path="*" element={<InputPage />} />
          </Routes>
        </div>

        <Popup 
            isShow={showPopup}
            setIsShow={setShowPopup}
            title={popupTitle}
            message={popupMessage}
            error={popupError}
            okCallback={popupOkCallBack}
          />
      </PopupContext.Provider>
    </DataContext.Provider>
  );
}

export default App;