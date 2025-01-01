import InputPageMatchingTheory from "../page/stableMatching/Input";
import { Route } from 'react-router-dom'
import InputProcessingPageMatchingTheory from "../page/stableMatching/Processing";
import MatchingTheoryOutputPage from "../page/stableMatching/Output";
import InputPage from "../page/gameTheory/Input";
import GuidePage from "../page/Guide";
import InputProcessingPage from "../page/gameTheory/Processing";
import OutputPage from "../page/gameTheory/Output";

export default function gameTheoryRouter() {
    return (
        <>
            <Route path="/" element={<InputPage />} />
            <Route path="/input" element={<InputPage />} />
            <Route path="/guide" element={<GuidePage />} />
            <Route path="/input-processing" element={<InputProcessingPage />} />
            <Route path="/result" element={<OutputPage />} />
        </>
    )
}