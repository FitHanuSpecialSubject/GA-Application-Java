import React, {useState} from 'react';
import './style.scss';

export default function Checkbox({ initialChecked, onChange }) {
  const [isChecked, setIsChecked] = useState(() => {
    return (initialChecked && typeof initialChecked === 'boolean')
        ? initialChecked : false;
  });

  const handleCheckboxChange = () => {
    setIsChecked((prevChecked) => !prevChecked);
    onChange(isChecked);
  };

  const suppressWarning = () => {
    //suppress development warning
  }

  return (
      <div className="checkbox-container" onClick={handleCheckboxChange}>
        <input type="checkbox"
               checked={isChecked}
               className="checkbox-input"
               onChange={suppressWarning}
        />
        <span className="checkbox-checkmark"></span>
      </div>
  );
}