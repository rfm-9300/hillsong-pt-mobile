#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

// Files and directories to clean up
const filesToRemove = [
  '.DS_Store',
  'node_modules/.cache',
  '.next/cache',
];

// Function to remove files/directories
function removeFile(filePath) {
  try {
    if (fs.existsSync(filePath)) {
      if (fs.lstatSync(filePath).isDirectory()) {
        fs.rmSync(filePath, { recursive: true, force: true });
        console.log(`Removed directory: ${filePath}`);
      } else {
        fs.unlinkSync(filePath);
        console.log(`Removed file: ${filePath}`);
      }
    }
  } catch (error) {
    console.error(`Error removing ${filePath}:`, error.message);
  }
}

// Function to find and remove .DS_Store files recursively
function removeDSStore(dir) {
  try {
    const files = fs.readdirSync(dir);
    
    files.forEach(file => {
      const filePath = path.join(dir, file);
      const stat = fs.lstatSync(filePath);
      
      if (stat.isDirectory()) {
        removeDSStore(filePath);
      } else if (file === '.DS_Store') {
        removeFile(filePath);
      }
    });
  } catch (error) {
    console.error(`Error processing directory ${dir}:`, error.message);
  }
}

console.log('Starting cleanup...');

// Remove specific files
filesToRemove.forEach(removeFile);

// Remove .DS_Store files recursively
removeDSStore('./src');
removeDSStore('./public');

console.log('Cleanup completed!');