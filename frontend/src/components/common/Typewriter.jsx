import React, { useState, useEffect } from 'react';

export default function Typewriter({ text, speed = 50, delay = 0, className = "" }) {
  const [displayText, setDisplayText] = useState('');
  const [cursorVisible, setCursorVisible] = useState(true);
  const [isTyping, setIsTyping] = useState(false);

  useEffect(() => {
    let timeout;
    if (delay > 0) {
      timeout = setTimeout(() => {
        setIsTyping(true);
        typeText();
      }, delay);
    } else {
      setIsTyping(true);
      typeText();
    }
    return () => clearTimeout(timeout);
  }, []);

  const typeText = () => {
    let index = 0;
    const interval = setInterval(() => {
      setDisplayText(text.substring(0, index + 1));
      index++;
      if (index >= text.length) {
        clearInterval(interval);
        setIsTyping(false);
      }
    }, speed);
  };

  useEffect(() => {
    const cursorInterval = setInterval(() => {
      setCursorVisible((v) => !v);
    }, 500);
    return () => clearInterval(cursorInterval);
  }, []);

  return (
    <span className={`inline-block ${className}`}>
      {displayText}
      <span 
        className={`inline-block w-[0.4em] h-[1em] bg-current ml-1 align-middle transition-opacity duration-100 ${
          cursorVisible || isTyping ? 'opacity-100' : 'opacity-0'
        }`}
      />
    </span>
  );
}
