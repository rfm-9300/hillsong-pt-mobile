import { User } from '../types';
import { 
  getUserDisplayName, 
  getUserInitials, 
  getUserAvatarColor, 
  isUserAdmin, 
  getUserRole 
} from '../userUtils';

// Mock user data for testing
const mockUserWithFullProfile: User = {
  id: '1',
  email: 'john.doe@example.com',
  verified: true,
  profile: {
    firstName: 'John',
    lastName: 'Doe',
    isAdmin: true
  }
};

const mockUserWithFirstNameOnly: User = {
  id: '2',
  email: 'jane@example.com',
  verified: false,
  profile: {
    firstName: 'Jane',
    isAdmin: false
  }
};

const mockUserWithEmailOnly: User = {
  id: '3',
  email: 'user@example.com',
  verified: true
};

describe('userUtils', () => {
  describe('getUserDisplayName', () => {
    it('should return full name when both first and last name are available', () => {
      expect(getUserDisplayName(mockUserWithFullProfile)).toBe('John Doe');
    });

    it('should return first name when only first name is available', () => {
      expect(getUserDisplayName(mockUserWithFirstNameOnly)).toBe('Jane');
    });

    it('should return email username when no profile name is available', () => {
      expect(getUserDisplayName(mockUserWithEmailOnly)).toBe('user');
    });
  });

  describe('getUserInitials', () => {
    it('should return initials from first and last name', () => {
      expect(getUserInitials(mockUserWithFullProfile)).toBe('JD');
    });

    it('should return first two characters when only one name is available', () => {
      expect(getUserInitials(mockUserWithFirstNameOnly)).toBe('JA');
    });

    it('should return initials from email username', () => {
      expect(getUserInitials(mockUserWithEmailOnly)).toBe('US');
    });
  });

  describe('getUserAvatarColor', () => {
    it('should return a consistent HSL color string', () => {
      const color = getUserAvatarColor(mockUserWithFullProfile);
      expect(color).toMatch(/^hsl\(\d+, 65%, 55%\)$/);
    });

    it('should return the same color for the same user', () => {
      const color1 = getUserAvatarColor(mockUserWithFullProfile);
      const color2 = getUserAvatarColor(mockUserWithFullProfile);
      expect(color1).toBe(color2);
    });
  });

  describe('isUserAdmin', () => {
    it('should return true for admin users', () => {
      expect(isUserAdmin(mockUserWithFullProfile)).toBe(true);
    });

    it('should return false for non-admin users', () => {
      expect(isUserAdmin(mockUserWithFirstNameOnly)).toBe(false);
    });

    it('should return false when profile is undefined', () => {
      expect(isUserAdmin(mockUserWithEmailOnly)).toBe(false);
    });
  });

  describe('getUserRole', () => {
    it('should return "Admin" for admin users', () => {
      expect(getUserRole(mockUserWithFullProfile)).toBe('Admin');
    });

    it('should return "User" for non-admin users', () => {
      expect(getUserRole(mockUserWithFirstNameOnly)).toBe('User');
    });

    it('should return "User" when profile is undefined', () => {
      expect(getUserRole(mockUserWithEmailOnly)).toBe('User');
    });
  });
});