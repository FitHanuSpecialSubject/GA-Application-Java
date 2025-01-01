import React from 'react';
import './style.scss';
export default function Loading({isLoading, message, estimatedTime, percentage}) {
    if (!isLoading) return (<></>);
    return (
        <div className='loading'>
            <div className="background"></div>
            <div class="lds-grid"><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div></div>
            <div class='loading-content'>
                {estimatedTime && <p className='estimated-time'>Total estimated {estimatedTime} minute(s) left</p>}
                {percentage && <p className='percentage'>{percentage}%</p>}
                {message && <p className='message'>{message}</p>}
            </div>
        </div>

    );
}
