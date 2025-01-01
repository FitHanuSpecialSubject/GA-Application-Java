import React from "react"
import "./style.scss";
import { useState } from 'react'
export default function Player({ index, name, strategies }) {
  const [showMore, setShowMore] = useState(false)

  const toggleShowMore = () => {
    setShowMore(!showMore)
  }

  return (
    <div className={`Player ${showMore ? 'show-more' : ''}`} onClick={toggleShowMore}>
      <div className="info">
        <div className="name">
          <p><span className="bold">#{index + 1}:</span> {name}</p>
          </div>
        <p className="strategyNum">{strategies.length} Strategies</p>
        <label className="show-more-btn">
        <span className={showMore ? 'fas fa-caret-up' : 'fas fa-caret-down'} onClick={toggleShowMore}></span>
        </label>  
      </div>
      {showMore && <div className="menubar">
        <ul>
          {strategies.map((strategy, index) => (
            <li className='strategy-name'> <span className="bold">Strategy #{index + 1}:</span> {strategy.name} ({strategy.properties.length} {strategy.properties.length > 1 ? 'Properties': 'Property'})</li>
          ))}
        </ul>

      </div>}


    </div>
  )
}


