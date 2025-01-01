import React from "react";
import "./style.scss";

export default function PlayerResult({ index, player}) {

  return (
    <div className="grid-item-container">
      <div className="column">#{index}</div>
      <div className="column player-name">{player.playerName}</div>
      <div className="column">{player.strategyName}</div>
      <div className="column">{player.payoff}</div>
    </div>
  );
}