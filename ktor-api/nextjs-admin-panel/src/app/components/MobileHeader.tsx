'use client';

import React from 'react';

interface MobileHeaderProps {
  onMenuClick: () => void;
}

const MobileHeader: React.FC<MobileHeaderProps> = ({ onMenuClick }) => {
  return (
    <header className="bg-gray-700 text-white p-4 flex justify-between items-center md:hidden animate-in slide-in-from-left" style={{ animationDuration: '300ms' }}>
      <h1 className="text-lg sm:text-xl font-bold">Admin Panel</h1>
      <button 
        onClick={onMenuClick} 
        className="text-white focus:outline-none touch-target p-2 rounded-md hover:bg-gray-600 transition-colors duration-200 active:scale-95"
        aria-label="Open navigation menu"
      >
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
        </svg>
      </button>
    </header>
  );
};

export default MobileHeader;
