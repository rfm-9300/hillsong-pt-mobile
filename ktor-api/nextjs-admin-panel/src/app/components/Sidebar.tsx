'use client';

import React from 'react';

const Sidebar: React.FC = () => {
  return (
    <aside className="w-64 bg-gray-800 text-white p-4 hidden md:block">
      <h2 className="text-2xl font-bold mb-4">Admin Panel</h2>
      <nav>
        <ul>
          <li className="mb-2"><a href="/admin/dashboard" className="hover:text-gray-300">Dashboard</a></li>
          <li className="mb-2"><a href="/admin/users" className="hover:text-gray-300">Users</a></li>
          <li className="mb-2"><a href="/admin/events" className="hover:text-gray-300">Events</a></li>
          <li className="mb-2"><a href="/admin/posts" className="hover:text-gray-300">Posts</a></li>
          <li className="mb-2"><a href="/admin/attendance" className="hover:text-gray-300">Attendance</a></li>
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;
