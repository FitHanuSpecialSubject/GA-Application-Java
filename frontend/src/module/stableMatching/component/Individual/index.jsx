import React from "react"
import "../../asset/css/styleIndividual.scss";
import { useState } from 'react'
export default function Individual({ index,allPropertyNames, IndividualName, Properties }) {
  const [showMore, setShowMore] = useState(false)

  const toggleShowMore = () => {
    setShowMore(!showMore)
  }

  return (
    <div className={`Player ${showMore ? 'show-more' : ''}`} onClick={toggleShowMore}>
      <div className="info">
        <div className="IndividualName">
            <p>
                <span className="bold">#{index + 1}:</span> {IndividualName}
            </p>
          </div>
        <p className="PropertiesNum">{Properties.length} Properties</p>
        <label className="show-more-btn">
            <span className={showMore ? 'fas fa-caret-up' : 'fas fa-caret-down'} onClick={toggleShowMore}></span>
        </label>  
      </div>
        {showMore && <div className="menubar">
        <ul>
            {Properties.map((Property, index) => (
                <li className='Property-name'> <span className="bold">{Object.values(allPropertyNames)[index]}:</span> Requirements: {Property[0]}; Weight: {Property[1]}; Personal Value: {Property[2]} </li>
            ))}
        </ul>
      </div>}
    </div>
  )
}


