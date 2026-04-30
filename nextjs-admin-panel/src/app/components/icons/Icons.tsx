import { SVGProps } from "react";

type Props = SVGProps<SVGSVGElement> & { size?: number };

function base({ size = 16, ...rest }: Props, d: string, d2?: string) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" {...rest}>
      <path d={d} stroke="currentColor" strokeWidth={1.7} strokeLinecap="round" strokeLinejoin="round" />
      {d2 && <path d={d2} stroke="currentColor" strokeWidth={1.7} strokeLinecap="round" strokeLinejoin="round" />}
    </svg>
  );
}

export const DashboardIcon = (p: Props) => base(p, "M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z", "M9 22V12h6v10");
export const PostsIcon = (p: Props) => base(p, "M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z", "M14 2v6h6M16 13H8M16 17H8M10 9H8");
export const EventsIcon = (p: Props) => base(p, "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z");
export const GroupsIcon = (p: Props) => base(p, "M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75", "M9 11a4 4 0 100-8 4 4 0 000 8z");
export const EncountersIcon = (p: Props) => base(p, "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z");
export const VideosIcon = (p: Props) => base(p, "M15 10l4.553-2.069A1 1 0 0121 8.845v6.309a1 1 0 01-1.447.894L15 14M3 8a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8z");
export const CalendarIcon = (p: Props) => base(p, "M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z");
export const UsersIcon = (p: Props) => base(p, "M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z");
export const AttendanceIcon = (p: Props) => base(p, "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4");
export const OverviewIcon = (p: Props) => base({ size: 14, ...p }, "M3 3h7v7H3V3zM14 3h7v7h-7V3zM14 14h7v7h-7v-7zM3 14h7v7H3v-7z");
export const ServiceIcon = (p: Props) => base({ size: 14, ...p }, "M12 2v10m0 0L8 8m4 4l4-4M5 17h14M3 21h18");
export const KidsIcon = (p: Props) => base({ size: 14, ...p }, "M12 14c-4.418 0-8 1.79-8 4v1h16v-1c0-2.21-3.582-4-8-4zM12 12a4 4 0 100-8 4 4 0 000 8z");
export const ReportsIcon = (p: Props) => base({ size: 14, ...p }, "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z");
export const SignoutIcon = (p: Props) => base(p, "M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1");
export const PlusIcon = (p: Props) => base({ size: 14, ...p }, "M12 5v14M5 12h14");
export const EditIcon = (p: Props) => base({ size: 13, ...p }, "M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7m-3.5-10.5a2.121 2.121 0 113 3L8 19H5v-3L18.5 2.5z");
export const TrashIcon = (p: Props) => base({ size: 13, ...p }, "M3 6h18m-2 0v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a1 1 0 011-1h4a1 1 0 011 1v2");
export const SearchIcon = (p: Props) => base({ size: 14, ...p }, "M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z");
export const ChevronRIcon = (p: Props) => base({ size: 14, ...p }, "M9 18l6-6-6-6");
export const ChevronDIcon = (p: Props) => base({ size: 14, ...p }, "M6 9l6 6 6-6");
export const RefreshIcon = (p: Props) => base({ size: 14, ...p }, "M4 4v5h5M20 20v-5h-5M4.09 9A8 8 0 1120 16.2");
export const CheckIcon = (p: Props) => base({ size: 13, ...p }, "M20 6L9 17l-5-5");
export const XIcon = (p: Props) => base({ size: 13, ...p }, "M18 6L6 18M6 6l12 12");
export const AlertIcon = (p: Props) => base({ size: 15, ...p }, "M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z", "M12 9v4M12 17h.01");
export const ImageIcon = (p: Props) => base(p, "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z");
export const EyeIcon = (p: Props) => base({ size: 13, ...p }, "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z", "M12 9a3 3 0 100 6 3 3 0 000-6z");
export const EyeOffIcon = (p: Props) => base({ size: 13, ...p }, "M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24M1 1l22 22");
export const FilterIcon = (p: Props) => base({ size: 14, ...p }, "M22 3H2l8 9.46V19l4 2v-8.54L22 3z");
export const DownloadIcon = (p: Props) => base({ size: 14, ...p }, "M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3");
export const GridIcon = (p: Props) => base({ size: 14, ...p }, "M3 3h7v7H3V3zM14 3h7v7h-7V3zM14 14h7v7h-7v-7zM3 14h7v7H3v-7z");
export const TableIcon = (p: Props) => base({ size: 14, ...p }, "M4 6h16M4 10h16M4 14h16M4 18h16");
