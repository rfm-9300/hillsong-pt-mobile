'use client';

import React from 'react';

interface MobileSidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const MobileSidebar: React.FC<MobileSidebarProps> = ({ isOpen, onClose }) => {
  return (
    <div
      className={`fixed inset-y-0 left-0 w-64 bg-gray-800 text-white p-4 transform ${isOpen ? 'translate-x-0' : '-translate-x-full'} transition-transform duration-300 ease-in-out md:hidden`}
    >
      <div className="flex justify-end mb-4">
        <button onClick={onClose} className="text-white focus:outline-none">
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>
      </div>
      <nav>
        <ul>
          <li className="mb-2"><a href="/admin/dashboard" className="hover:text-gray-300">Dashboard</a></li>
          <li className="mb-2"><a href="/admin/users" className="hover:text-gray-300">Users</a></li>
          <li className="mb-2"><a href="/admin/events" className="hover:text-gray-300">Events</a></li>
          <li className="mb-2"><a href="/admin/posts" className="hover:text-gray-300">Posts</a></li>
          <li className="mb-2"><a href="/admin/attendance" className="hover:text-gray-300">Attendance</a></li>
        </ul>
      </nav>
    </div>
  );
};

export default MobileSidebar;
