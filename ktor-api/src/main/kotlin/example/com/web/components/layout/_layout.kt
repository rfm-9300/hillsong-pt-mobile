package example.com.web.components.layout


import example.com.routes.Routes
import example.com.web.loadHeaderScripts
import kotlinx.html.*

fun HTML.layout(e: BODY.() -> Unit) {
    head {
        // Mobile viewport meta tag for responsive design
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no")
        
        // Site title
        title("Active Hive | Event Management Platform")
        
        // Favicon
        link(rel = "icon", href = "/resources/images/favicon.ico", type = "image/x-icon")
        link(rel = "shortcut icon", href = "/resources/images/favicon.ico", type = "image/x-icon")
        // Additional favicon formats for better browser support
        link(rel = "apple-touch-icon", href = "/resources/images/apple-touch-icon.png")
        link(rel = "icon", type = "image/png", href = "/resources/images/default-user-image.webp") {
            attributes["sizes"] = "32x32"
        }
        link(rel = "icon", type = "image/png", href = "/resources/images/default-user-image.webp") {
            attributes["sizes"] = "16x16"
        }
        
        // Add Tailwind CSS CDN
        script(src = "https://cdn.tailwindcss.com") {}
        script (src = Routes.DynamicJs.API_CLIENT){}
        // cropper.js
        script(src = "https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.js") {}
        link (rel = "stylesheet", href = "https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.css")
        loadHeaderScripts()
        
        // Custom responsive styles
        style {
            +"""
            /* Global responsive styles */
            html, body {
                -webkit-overflow-scrolling: touch;
                scroll-behavior: smooth;
                overscroll-behavior-y: none;
            }
            
            /* Hide scrollbars but allow scrolling */
            .hide-scrollbar::-webkit-scrollbar {
                display: none;
            }
            .hide-scrollbar {
                -ms-overflow-style: none;
                scrollbar-width: none;
            }
            
            /* Touch-friendly button size */
            button, a, .clickable {
                min-height: 36px;
                touch-action: manipulation;
            }
            
            /* Font size adjustments for mobile */
            @media (max-width: 640px) {
                body {
                    font-size: 14px;
                }
                h1 {
                    font-size: 1.5rem !important;
                }
                h2 {
                    font-size: 1.25rem !important;
                }
                .mobile-text-sm {
                    font-size: 0.875rem !important;
                }
            }
            
            /* Avoid text overflow */
            p, h1, h2, h3, h4, h5, h6, span {
                overflow-wrap: break-word;
                word-wrap: break-word;
                -ms-word-break: break-word;
                word-break: break-word;
            }
            """
        }
    }

    body {
        // Add responsive body class
        attributes["class"] = "touch-manipulation text-black antialiased"
        e()
    }
}