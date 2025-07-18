<script>
    import { AttendanceStatus } from '../../types/attendance';
    
    // Props
    let {
        status,
        size = 'md',
        showIcon = true,
        class: className = '',
        ...props
    } = $props();
    
    // Size variants
    const sizes = {
        sm: 'px-1.5 py-0.5 text-xs',
        md: 'px-2 py-1 text-xs',
        lg: 'px-3 py-1.5 text-sm'
    };
    
    // Get status info based on status
    function getStatusInfo(status) {
        switch (status) {
            case AttendanceStatus.CHECKED_IN:
                return {
                    badgeClass: 'bg-green-100 text-green-800',
                    iconClass: 'text-green-500',
                    icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z', // Check circle
                    label: 'Checked In'
                };
            case AttendanceStatus.CHECKED_OUT:
                return {
                    badgeClass: 'bg-blue-100 text-blue-800',
                    iconClass: 'text-blue-500',
                    icon: 'M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4', // Archive box
                    label: 'Checked Out'
                };
            case AttendanceStatus.EMERGENCY:
                return {
                    badgeClass: 'bg-red-100 text-red-800',
                    iconClass: 'text-red-500',
                    icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z', // Exclamation triangle
                    label: 'Emergency'
                };
            case AttendanceStatus.NO_SHOW:
                return {
                    badgeClass: 'bg-gray-100 text-gray-800',
                    iconClass: 'text-gray-500',
                    icon: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z', // X circle
                    label: 'No Show'
                };
            default:
                return {
                    badgeClass: 'bg-gray-100 text-gray-800',
                    iconClass: 'text-gray-500',
                    icon: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z', // Question mark circle
                    label: 'Unknown'
                };
        }
    }
    
    const statusInfo = getStatusInfo(status);
</script>

<span 
    class="inline-flex items-center gap-1 leading-5 font-semibold rounded-full {sizes[size]} {statusInfo.badgeClass} {className}"
    {...props}
>
    {#if showIcon}
        <svg class="h-4 w-4 {statusInfo.iconClass}" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d={statusInfo.icon} />
        </svg>
    {/if}
    {statusInfo.label}
</span>