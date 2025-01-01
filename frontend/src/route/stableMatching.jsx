import InputPageMatchingTheory from "../page/stableMatching/Input";
import { Route } from 'react-router-dom'
import InputProcessingPageMatchingTheory from "../page/stableMatching/Processing";
import MatchingTheoryOutputPage from "../page/stableMatching/Output";

export default function stableMatchingRouter() {
    return (
        <>
            <Route path="/matching-theory/" element={<InputPageMatchingTheory />} />
            <Route path="/matching-theory/input" element={<InputPageMatchingTheory />} />
            <Route path="/matching-theory/input-processing" element={<InputProcessingPageMatchingTheory />} />
            <Route path="/matching-theory/result" element={< MatchingTheoryOutputPage />} />
        </>
    )
}