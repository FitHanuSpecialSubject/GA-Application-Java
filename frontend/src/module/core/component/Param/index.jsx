import React from 'react';
import "./style.scss";
import InputHint from '../InputHint';
import { useState, useContext } from 'react';

export default function ({ inputText, paramVal, setParamVal, hintTitle, hintContent }) {
    // const playerHolder = error ? message: message
    const [showHint, setShowHint] = useState(false);

    const handleMouseOver = () => {
        setShowHint(true);

    }
    const handleMouseLeave = () => {
        setShowHint(false);

    }

    return (
        <div className='Param'>
            <InputHint
                showHint={showHint}
                setShowHint={setShowHint}
                heading={hintTitle}
                description={hintContent}
                guideSectionIndex={8}
            />
            <div className='text'>
                <i className="info fa-solid fa-info" onMouseOver={handleMouseOver} onMouseLeave={handleMouseLeave}></i>
                <p className="bold">{inputText}</p>
            </div>

            <input type="number" className='param-input' value={paramVal} onChange={e => setParamVal(e.target.value)} />


        </div>

    )
}