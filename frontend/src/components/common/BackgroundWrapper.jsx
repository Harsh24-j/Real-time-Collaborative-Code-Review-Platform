import React from 'react'

/**
 * Background Wrapper Component
 * Provides different background styles for different pages
 * Skills: HTML/CSS, Responsive Web Design
 */
function BackgroundWrapper({ children, variant = 'code1' }) {
  const backgrounds = {
    // Code background 1 - Laptop with code
    code1: (
      <div className="fixed inset-0 bg-code-1 -z-10">
        <div className="overlay-grid h-full w-full" />
      </div>
    ),

    // Code background 2 - Programming workspace
    code2: (
      <div className="fixed inset-0 bg-code-2 -z-10">
        <div className="overlay-dots h-full w-full" />
      </div>
    ),

    // Abstract tech background
    abstract: (
      <div className="fixed inset-0 bg-abstract-1 -z-10 bg-image-animated" />
    ),

    // Dynamic Register background (slow zoom + dark tech image + particles)
    register: (
      <div className="fixed inset-0 overflow-hidden -z-10 bg-black">
        {/* Scale up from a bleed area to prevent edges from showing during zoom */}
        <div className="absolute -inset-[5%] bg-register-image bg-zoom-animated" />
        <div className="absolute inset-0 particles-bg mix-blend-screen opacity-50" />
      </div>
    ),

    // Dark tech theme
    darkTech: (
      <div className="fixed inset-0 bg-tech-dark -z-10">
        {/* Floating particles */}
        <div className="absolute top-20 left-10 w-64 h-64 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse" />
        <div className="absolute top-40 right-10 w-64 h-64 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse animation-delay-2000" />
        <div className="absolute bottom-20 left-1/2 w-64 h-64 bg-pink-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse animation-delay-4000" />
      </div>
    ),

    // Gradient mesh with image
    mesh: (
      <div className="fixed inset-0 bg-gradient-mesh -z-10" />
    ),

    // Pure gradient (no image)
    gradient: (
      <div className="fixed inset-0 bg-animated-gradient -z-10" />
    ),

    // Custom image (pass URL via data attribute)
    custom: (
      <div 
        className="fixed inset-0 -z-10"
        style={{
          backgroundImage: `linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0.4)), url('https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1920')`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          backgroundAttachment: 'fixed'
        }}
      />
    ),

    // Minimal with subtle pattern
    minimal: (
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-gray-50 to-gray-100">
        <svg className="absolute w-full h-full opacity-5" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="grid-pattern" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="currentColor" strokeWidth="1"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid-pattern)" />
        </svg>
      </div>
    ),

    // Waves with image
    waves: (
      <div className="fixed inset-0 -z-10">
        <div 
          className="absolute inset-0"
          style={{
            backgroundImage: `url('https://images.unsplash.com/photo-1557682224-5b8590cd9ec5?w=1920')`,
            backgroundSize: 'cover',
            backgroundPosition: 'center'
          }}
        />
        <div className="absolute inset-0 bg-gradient-to-br from-blue-500/80 via-purple-500/80 to-pink-500/80" />
        <svg className="absolute bottom-0 w-full opacity-20" viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path
            fill="rgba(255,255,255,1)"
            fillOpacity="1"
            d="M0,96L48,112C96,128,192,160,288,186.7C384,213,480,235,576,213.3C672,192,768,128,864,128C960,128,1056,192,1152,197.3C1248,203,1344,149,1392,122.7L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
          />
        </svg>
      </div>
    ),
    
    // original particles fallback for existing configs
    particles: (
      <div className="fixed inset-0 particles-bg -z-10">
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-950 via-purple-950 to-pink-950" />
      </div>
    )
  }

  return (
    <>
      {backgrounds[variant] || backgrounds.code1}
      <div className="relative min-h-screen">
        {children}
      </div>
    </>
  )
}

export default BackgroundWrapper
