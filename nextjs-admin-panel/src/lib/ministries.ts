import { z } from 'zod';
import { Ministry } from './types';
import { ministryOptions } from './groups';

const locationSchema = z.object({
  addressLine: z.string().trim().min(1, 'Address is required'),
  city: z.string().trim().min(1, 'City is required'),
  region: z.string().trim().optional(),
  postalCode: z.string().trim().optional(),
  country: z.string().trim().length(2, 'Country must be a 2-letter code'),
  latitude: z.coerce.number().min(-90).max(90),
  longitude: z.coerce.number().min(-180).max(180),
});

export const ministryFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(120),
  type: z.nativeEnum(Ministry),
  description: z.string().trim().min(1, 'Description is required').max(2000),
  isActive: z.boolean(),
  isJoinable: z.boolean(),
  requiresApproval: z.boolean(),
  tags: z.string(),
  location: locationSchema.optional(),
  hasLocation: z.boolean(),
});

export type MinistryFormValues = z.infer<typeof ministryFormSchema>;

export const defaultMinistryFormValues: MinistryFormValues = {
  name: '',
  type: Ministry.GERAL,
  description: '',
  isActive: true,
  isJoinable: true,
  requiresApproval: true,
  tags: '',
  location: undefined,
  hasLocation: false,
};

export const formatMinistryType = (value: Ministry): string =>
  ministryOptions.find((option) => option.value === value)?.label ?? value;
