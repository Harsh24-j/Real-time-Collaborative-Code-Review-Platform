import React from 'react';

export default function FloatingOrbs() {
  return (
    <div className="absolute inset-0 overflow-hidden pointer-events-none z-[-1]">
      <div className="absolute top-[10%] left-[15%] w-16 h-16 rounded-full bg-cyan-400/20 blur-xl animate-float" />
      <div className="absolute top-[60%] left-[5%] w-24 h-24 rounded-full bg-purple-500/20 blur-2xl animate-float animation-delay-2000" />
      <div className="absolute top-[20%] right-[10%] w-32 h-32 rounded-full bg-pink-500/10 blur-3xl animate-float animation-delay-4000" />
      <div className="absolute bottom-[10%] right-[20%] w-20 h-20 rounded-full bg-indigo-500/20 blur-xl animate-float animation-delay-2000 p-8 flex items-center justify-center">
        <span className="text-white/20 text-4xl filter drop-shadow-md">{"</>"}</span>
      </div>
      <div className="absolute top-[40%] left-[80%] w-16 h-16 rounded-full bg-blue-500/20 blur-xl animate-float p-4 flex items-center justify-center">
        <span className="text-white/20 text-3xl filter drop-shadow-md">{"{ }"}</span>
      </div>
    </div>
  );
}
