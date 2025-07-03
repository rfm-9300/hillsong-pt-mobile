package example.com.web.components

import kotlinx.html.*

fun HtmlBlockTag.svgIcon(icon: SvgIcon, classes: String = "", size: Int = 24) {
    span {
        unsafe {
            when (icon) {
                SvgIcon.CHEVRON_DOWN ->
                    +"""
            <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 8l7 7 7-7"/>
            </svg>
            """
                SvgIcon.FACEBOOK -> +"""
<svg width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M2,2 v10 M2,2 h2 M2,7 h4"/>
</svg>
"""
                SvgIcon.X -> +"""
<svg width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"/>
</svg>
"""
                SvgIcon.MENU -> +"""
                <svg class="$classes" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                </svg>
            """
                SvgIcon.CLOSE -> +"""
                <svg class="$classes" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            """
                SvgIcon.SEARCH -> +"""
                <svg class="$classes" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
            """
                SvgIcon.LIKE -> +"""
                <svg class="$classes" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M12 6.00019C10.2006 3.90317 7.19377 3.2551 4.93923 5.17534C2.68468 7.09558 2.36727 10.3061 4.13778 12.5772C5.60984 14.4654 10.0648 18.4479 11.5249 19.7369C11.6882 19.8811 11.7699 19.9532 11.8652 19.9815C11.9483 20.0062 12.0393 20.0062 12.1225 19.9815C12.2178 19.9532 12.2994 19.8811 12.4628 19.7369C13.9229 18.4479 18.3778 14.4654 19.8499 12.5772C21.6204 10.3061 21.3417 7.07538 19.0484 5.17534C16.7551 3.2753 13.7994 3.90317 12 6.00019Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            """
                SvgIcon.TIME -> +"""
<svg class="$classes" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
    <path d="M12 8V12L14.5 14.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
                                    """
                SvgIcon.DEFAULT -> +"""
                <svg class="$classes" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <rect x="4" y="4" width="16" height="16" rx="2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            """
                SvgIcon.DELETE -> +"""
                <svg width='24' height='24' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'><rect width='24' height='24' stroke='none' fill='#000000' opacity='0'/>
                    <g transform="matrix(0.5 0 0 0.5 12 12)" >
                    <g style="" >
                    <g transform="matrix(1 0 0 1 1 0)" >
                    <path style="stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-dashoffset: 0; stroke-linejoin: miter; stroke-miterlimit: 4; fill: rgb(223,240,254); fill-rule: nonzero; opacity: 1;" transform=" translate(-21, -20)" d="M 21 24.15 L 8.857 36.293 L 4.707 32.143 L 16.85 20 L 4.707 7.857 L 8.857 3.707 L 21 15.85 L 33.143 3.707 L 37.293 7.857 L 25.15 20 L 37.293 32.143 L 33.143 36.293 z" stroke-linecap="round" />
                    </g>
                    <g transform="matrix(1 0 0 1 1 0)" >
                    <path style="stroke: none; stroke-width: 1; stroke-dasharray: none; stroke-linecap: butt; stroke-dashoffset: 0; stroke-linejoin: miter; stroke-miterlimit: 4; fill: rgb(71,136,199); fill-rule: nonzero; opacity: 1;" transform=" translate(-21, -20)" d="M 33.143 4.414 L 36.586 7.856999999999999 L 25.15 19.293 L 24.443 20 L 25.150000000000002 20.707 L 36.586 32.143 L 33.143 35.586 L 21.707 24.15 L 21 23.443 L 20.293 24.150000000000002 L 8.857 35.586 L 5.414 32.143 L 16.85 20.707 L 17.557 20 L 16.849999999999998 19.293 L 5.414 7.857 L 8.857 4.414 L 20.293 15.85 L 21 16.557 L 21.707 15.849999999999998 L 33.143 4.414 M 33.143 3 L 21 15.143 L 8.857 3 L 4 7.857 L 16.143 20 L 4 32.143 L 8.857 37 L 21 24.857 L 33.143 37 L 38 32.143 L 25.857 20 L 38 7.857 L 33.143 3 L 33.143 3 z" stroke-linecap="round" />
                    </g>
                    </g>
                    </g>
                    </svg>
                        """
                SvgIcon.EDIT -> +"""
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18.3785 8.44975L11.4637 15.3647C11.1845 15.6439 10.8289 15.8342 10.4417 15.9117L7.49994 16.5L8.08829 13.5582C8.16572 13.1711 8.35603 12.8155 8.63522 12.5363L15.5501 5.62132M18.3785 8.44975L19.7927 7.03553C20.1832 6.64501 20.1832 6.01184 19.7927 5.62132L18.3785 4.20711C17.988 3.81658 17.3548 3.81658 16.9643 4.20711L15.5501 5.62132M18.3785 8.44975L15.5501 5.62132" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M5 20H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                    """
                SvgIcon.CALENDAR -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                """
                SvgIcon.ARROW_LEFT -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                </svg>
                """
                SvgIcon.HOME -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2v-4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                </svg>
                """
                SvgIcon.PROFILE -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
                """
                SvgIcon.SETTINGS -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                </svg>
                """
                SvgIcon.LOGOUT -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                """
                SvgIcon.CHECK_CIRCLE -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                """
                SvgIcon.BEE -> +"""
                    <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg" fill="none" stroke="black" stroke-width="4" stroke-linecap="round" stroke-linejoin="round">
    <!-- Bee Body -->
    <ellipse cx="80" cy="100" rx="40" ry="30" fill="#FFD700" stroke="black"/>
    <line x1="60" y1="85" x2="100" y2="85" stroke="black" stroke-width="4"/>
    <line x1="60" y1="100" x2="100" y2="100" stroke="black" stroke-width="4"/>
    <line x1="60" y1="115" x2="100" y2="115" stroke="black" stroke-width="4"/>
    
    <!-- Bee Head -->
    <circle cx="40" cy="100" r="15" fill="#FFD700" stroke="black"/>
    <circle cx="35" cy="95" r="3" fill="black"/>
    <circle cx="45" cy="95" r="3" fill="black"/>
    
    <!-- Antennae -->
    <line x1="30" y1="85" x2="25" y2="70" stroke="black"/>
    <line x1="50" y1="85" x2="55" y2="70" stroke="black"/>
    
    <!-- Wings -->
    <ellipse cx="90" cy="80" rx="20" ry="10" fill="white" stroke="black"/>
    <ellipse cx="90" cy="120" rx="20" ry="10" fill="white" stroke="black"/>
    
    <!-- Volleyball -->
    <circle cx="140" cy="100" r="20" fill="white" stroke="black"/>
    <path d="M122,110 Q140,90 158,110" stroke="black" fill="none"/>
    <path d="M140,80 Q150,100 130,120" stroke="black" fill="none"/>
</svg>
                """
                SvgIcon.LOCATION -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
                    <circle cx="12" cy="9" r="2.5"/>
                </svg>
                """
                SvgIcon.EMAIL -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                    <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                """
                SvgIcon.LOCK -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                """
                SvgIcon.LOGIN -> +"""
                <svg class="$classes" width="$size" height="$size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                    <polyline points="10 17 15 12 10 7"></polyline>
                    <line x1="15" y1="12" x2="3" y2="12"></line>
                </svg>
                """
            }
        }
    }
}

enum class SvgIcon {
    BEE, MENU, CLOSE, SEARCH, DEFAULT, LIKE, TIME, DELETE, EDIT, FACEBOOK, X, CHEVRON_DOWN, CALENDAR, ARROW_LEFT, HOME, PROFILE, SETTINGS, LOGOUT, CHECK_CIRCLE, LOCATION, EMAIL, LOCK, LOGIN
}