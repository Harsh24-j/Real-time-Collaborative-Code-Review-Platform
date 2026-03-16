import React, { useEffect, useState } from 'react';

export default function MouseGlow() {
  const [pos, setPos] = useState({ x: -1000, y: -1000 });
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const handleMouseMove = (e) => {
      setPos({ x: e.clientX, y: e.clientY });
      if (!isVisible) setIsVisible(true);
    };
    
    const handleMouseLeave = () => setIsVisible(false);

    window.addEventListener('mousemove', handleMouseMove);
    document.body.addEventListener('mouseleave', handleMouseLeave);

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      document.body.removeEventListener('mouseleave', handleMouseLeave);
    };
  }, [isVisible]);

  return (
    <div 
      className="pointer-events-none fixed inset-0 z-0 overflow-hidden transition-opacity duration-700"
      style={{ opacity: isVisible ? 1 : 0 }}
    >
      {/* Primary bright glow tracking cursor closely */}
      <div 
        className="absolute w-[400px] h-[400px] rounded-full bg-blue-400/20 mix-blend-screen filter blur-[100px] transition-transform duration-100 ease-out"
        style={{ transform: `translate(${pos.x - 200}px, ${pos.y - 200}px)` }}
      />
      {/* Secondary colored glow lagging slightly behind cursor */}
      <div 
        className="absolute w-[300px] h-[300px] rounded-full bg-fuchsia-500/20 mix-blend-screen filter blur-[80px] transition-transform duration-300 ease-out"
        style={{ transform: `translate(${pos.x - 150}px, ${pos.y - 150}px)` }}
      />
    </div>
  );
}
