import { User } from './types';

/**
 * Generate user display name with fallbacks
 */
export function getUserDisplayName(user: User): string {
  if (user.profile?.firstName && user.profile?.lastName) {
    return `${user.profile.firstName} ${user.profile.lastName}`;
  }
  
  if (user.profile?.firstName) {
    return user.profile.firstName;
  }
  
  if (user.profile?.lastName) {
    return user.profile.lastName;
  }
  
  // Fallback to email username part
  return user.email.split('@')[0];
}

/**
 * Generate initials from user name or email
 */
export function getUserInitials(user: User): string {
  const displayName = getUserDisplayName(user);
  
  // If display name has spaces, use first letter of each word
  const words = displayName.split(' ');
  if (words.length > 1) {
    return words.slice(0, 2).map(word => word.charAt(0).toUpperCase()).join('');
  }
  
  // Otherwise use first two characters
  return displayName.slice(0, 2).toUpperCase();
}

/**
 * Generate a consistent color based on user name
 */
export function getUserAvatarColor(user: User): string {
  const displayName = getUserDisplayName(user);
  
  // Simple hash function to generate consistent color
  let hash = 0;
  for (let i = 0; i < displayName.length; i++) {
    hash = displayName.charCodeAt(i) + ((hash << 5) - hash);
  }
  
  // Convert to hue value (0-360)
  const hue = Math.abs(hash) % 360;
  
  // Return HSL color with good saturation and lightness for readability
  return `hsl(${hue}, 65%, 55%)`;
}

/**
 * Check if user is admin
 */
export function isUserAdmin(user: User): boolean {
  return user.profile?.isAdmin ?? false;
}

/**
 * Get user role display text
 */
export function getUserRole(user: User): string {
  return isUserAdmin(user) ? 'Admin' : 'User';
}