-- V11__Add_service_date_to_kids_service.sql
-- Add service_date column to kids_service table

-- Add the service_date column
ALTER TABLE public.kids_service 
ADD COLUMN IF NOT EXISTS service_date DATE NOT NULL DEFAULT CURRENT_DATE;

-- Update existing records with appropriate service dates
-- Set service dates to next occurrence of the day_of_week
UPDATE public.kids_service 
SET service_date = CASE 
    WHEN day_of_week = 'SUNDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 0 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'MONDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 1 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'TUESDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 2 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'WEDNESDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 3 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'THURSDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 4 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'FRIDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 5 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    WHEN day_of_week = 'SATURDAY' THEN 
        CURRENT_DATE + INTERVAL '1 day' * ((7 + 6 - EXTRACT(DOW FROM CURRENT_DATE)::INTEGER) % 7)
    ELSE CURRENT_DATE
END
WHERE service_date = CURRENT_DATE; -- Only update records that still have the default value

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_kids_service_service_date ON public.kids_service(service_date);
CREATE INDEX IF NOT EXISTS idx_kids_service_day_date ON public.kids_service(day_of_week, service_date);