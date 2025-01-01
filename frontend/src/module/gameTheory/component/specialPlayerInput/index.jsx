import React from 'react';
import "./style.scss";
import Input from "../../../core/component/input";
import { useState, useEffect } from 'react';

export default function SpecialPlayerInput({ specialPlayerExists, setSpecialPlayerExists, specialPlayerPropsNum, setSpecialPlayerPropsNum, error }) {
    const [style, setStyle] = useState("hidden");


    const handleHidingAnimationEnd = (e) => {
        if (style === 'hiding') {
            e.target.classList.add('hidden');
        } else {
            e.target.classList.remove('hidden');
        }

    }

    useEffect(() => {
        if (specialPlayerExists) {
            setStyle('showing');
        } else {
            setStyle('hidden');
        }
    }, [specialPlayerExists]);


  
    return (
        <div className={`special-player-input`}>
            <label htmlFor="special-player-checkbox" className='special-player-label'>
                <input
                    type="checkbox"
                    placeholder="You problem name"
                    id="special-player-checkbox"
                    onChange={e => setSpecialPlayerExists(e.target.checked)}
                    value={specialPlayerExists}

                />
                <p id='text'>Special player exists</p>
            </label>

            <div className={`${style}`} onAnimationEnd={handleHidingAnimationEnd}>
                <Input
                    message="Number of properties of special player"
                    type='number'
                    error={error}
                    handleOnChange={(e) => {
                        setSpecialPlayerPropsNum(e.target.value)
                    }}
                    value={specialPlayerPropsNum}
                    description="Positive integer value that corresponds to the number of properties that the special player has."
                    guideSectionIndex={3}
                />
            </div>

        </div>
    )
}