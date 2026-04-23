import { z } from 'zod';
import { GroupDayOfWeek, MeetingFrequency, Ministry } from './types';

export const ministryOptions: Array<{ value: Ministry; label: string }> = [
  { value: Ministry.SISTERHOOD, label: 'Sisterhood' },
  { value: Ministry.JOVENS_YXYA, label: 'Jovens YxYa' },
  { value: Ministry.MENS, label: 'Homens' },
  { value: Ministry.CASAIS, label: 'Casais' },
  { value: Ministry.THIRTY_PLUS, label: '30+' },
  { value: Ministry.GERAL, label: 'Geral' },
];

export const frequencyOptions: Array<{ value: MeetingFrequency; label: string }> = [
  { value: MeetingFrequency.WEEKLY, label: 'Weekly' },
  { value: MeetingFrequency.BIWEEKLY, label: 'Biweekly' },
  { value: MeetingFrequency.MONTHLY, label: 'Monthly' },
];

export const dayOptions: Array<{ value: GroupDayOfWeek; label: string }> = [
  { value: GroupDayOfWeek.MONDAY, label: 'Monday' },
  { value: GroupDayOfWeek.TUESDAY, label: 'Tuesday' },
  { value: GroupDayOfWeek.WEDNESDAY, label: 'Wednesday' },
  { value: GroupDayOfWeek.THURSDAY, label: 'Thursday' },
  { value: GroupDayOfWeek.FRIDAY, label: 'Friday' },
  { value: GroupDayOfWeek.SATURDAY, label: 'Saturday' },
  { value: GroupDayOfWeek.SUNDAY, label: 'Sunday' },
];

export const cityOptions = [
  'Lisboa',
  'Porto',
  'Cascais',
  'Almada',
  'Braga',
  'Coimbra',
  'Faro',
  'Outros',
] as const;

const locationSchema = z.object({
  addressLine: z.string().trim().min(1, 'Address is required'),
  city: z.string().trim().min(1, 'City is required'),
  region: z.string().trim().optional(),
  postalCode: z.string().trim().optional(),
  country: z.string().trim().length(2, 'Country must be a 2-letter code'),
  latitude: z.coerce.number().min(-90, 'Latitude must be >= -90').max(90, 'Latitude must be <= 90'),
  longitude: z.coerce.number().min(-180, 'Longitude must be >= -180').max(180, 'Longitude must be <= 180'),
});

export const groupFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(120, 'Name must be at most 120 characters'),
  ministry: z.nativeEnum(Ministry),
  description: z.string().trim().min(1, 'Description is required').max(2000, 'Description must be at most 2000 characters'),
  leaderName: z.string().trim().min(1, 'Leader name is required').max(120, 'Leader name must be at most 120 characters'),
  leaderContact: z.string().trim().min(1, 'Leader contact is required').max(60, 'Leader contact must be at most 60 characters'),
  meetingDay: z.nativeEnum(GroupDayOfWeek),
  meetingTime: z.string().trim().regex(/^\d{2}:\d{2}$/, 'Meeting time must be in HH:mm format'),
  frequency: z.nativeEnum(MeetingFrequency),
  location: locationSchema,
  maxMembers: z.union([z.literal(''), z.coerce.number().int().min(1, 'Max members must be at least 1').max(1000, 'Max members must be at most 1000')]),
  currentMembers: z.coerce.number().int().min(0, 'Current members must be 0 or more').max(10000, 'Current members must be at most 10000'),
  isActive: z.boolean(),
  isJoinable: z.boolean(),
  tags: z.string(),
});

export type GroupFormValues = z.infer<typeof groupFormSchema>;

export const defaultGroupFormValues: GroupFormValues = {
  name: '',
  ministry: Ministry.GERAL,
  description: '',
  leaderName: '',
  leaderContact: '',
  meetingDay: GroupDayOfWeek.THURSDAY,
  meetingTime: '20:00',
  frequency: MeetingFrequency.WEEKLY,
  location: {
    addressLine: '',
    city: cityOptions[0],
    region: '',
    postalCode: '',
    country: 'PT',
    latitude: 38.7223,
    longitude: -9.1393,
  },
  maxMembers: '',
  currentMembers: 0,
  isActive: true,
  isJoinable: true,
  tags: '',
};

export const formatGroupDay = (value: GroupDayOfWeek): string =>
  dayOptions.find((option) => option.value === value)?.label ?? value;

export const formatGroupFrequency = (value: MeetingFrequency): string =>
  frequencyOptions.find((option) => option.value === value)?.label ?? value;

export const formatMinistry = (value: Ministry): string =>
  ministryOptions.find((option) => option.value === value)?.label ?? value;
