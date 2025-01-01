import React from 'react';
import "./style.scss";
import { Link } from 'react-router-dom'
import { useState, useContext } from 'react';
import DataContext from '../../context/DataContext';

export default function InputHint({ showHint, setShowHint, heading, description, guideSectionIndex }) {
// onMouseLeave={setShowHint(false)}
    const { setGuideSectionIndex } = useContext(DataContext);
    // let shortDescription = description.split(' ').slice(0, 15).join(' ');

    // if (description.split(' ').length > 15) {
    //     shortDescription += '...';
    // }
    const handleMouseLeave = () => {
        setShowHint(false);
    }
    return (

        showHint &&

        <div className="input-hint" onMouseLeave={handleMouseLeave}>
            <h1>{heading}</h1>
            <p>{description}</p>
            <Link to='/guide' className='btn' onClick={e => setGuideSectionIndex(guideSectionIndex)}> Learn more</Link>
        </div>


    )
}