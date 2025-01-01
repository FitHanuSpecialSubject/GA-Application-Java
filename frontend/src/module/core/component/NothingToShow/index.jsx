
import React from 'react';
import './style.scss';
import { useNavigate } from 'react-router-dom';
import AstronautImage from '../../asset/image/astronaut.png'
export default function NothingToShow() {
    const navigate = useNavigate();

    const navigateToHome = () => {
        navigate('/')
    }

    return (
        <div className='nothing-to-show'>
            <p>Nothing to show here</p>
            <img src={AstronautImage} alt="" />
            <button className='btn' onClick={navigateToHome}>Back to homepage</button>
        </div>
    );
}
