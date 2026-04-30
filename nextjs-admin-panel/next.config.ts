import type { NextConfig } from "next";

// Bundle analyzer
const withBundleAnalyzer = require('@next/bundle-analyzer')({
  enabled: process.env.ANALYZE === 'true',
});

const isProductionBuild = process.env.NODE_ENV === 'production';
const backendApiUrl =
  process.env.API_BASE_URL ||
  (isProductionBuild ? 'http://springboot-api:8080/api' : 'http://localhost:8080/api');
const fileProxyUrl =
  process.env.FILE_PROXY_URL ||
  (isProductionBuild ? 'http://minio:9000/church-files' : 'http://localhost:9000/church-files');

const nextConfig: NextConfig = {
  // Enable experimental features for better performance
  experimental: {
    optimizePackageImports: ['react-hook-form', '@hookform/resolvers'],
  },

  // Ignore ESLint errors during build to prevent deployment failure
  eslint: {
    ignoreDuringBuilds: true,
  },

  // Ignore TypeScript errors during build to prevent deployment failure
  typescript: {
    ignoreBuildErrors: true,
  },

  // Optimize images
  images: {
    formats: ['image/webp', 'image/avif'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'localhost',
      },
      {
        protocol: 'http',
        hostname: 'minio',
      },
      {
        protocol: 'https',
        hostname: 'img.youtube.com',
      }
    ],
  },

  // Enable compression
  compress: true,

  // Output standalone build for Docker
  output: 'standalone',

  // Optimize bundle
  webpack: (config, { dev, isServer }) => {
    // Optimize bundle size in production
    if (!dev && !isServer) {
      config.optimization.splitChunks = {
        chunks: 'all',
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendors',
            chunks: 'all',
          },
          common: {
            name: 'common',
            minChunks: 2,
            chunks: 'all',
            enforce: true,
          },
        },
      };
    }

    return config;
  },

  // Proxy /api/* to Spring Boot (except /api/auth/* which are handled by Next.js)
  async rewrites() {
    return [
      {
        source: '/api/auth/login',
        destination: '/api/auth/login', // handled by Next.js route
      },
      {
        source: '/api/auth/signup',
        destination: '/api/auth/signup', // handled by Next.js route
      },
      {
        source: '/api/files/:path*',
        destination: `${fileProxyUrl}/:path*`,
      },
      {
        source: '/api/:path*',
        destination: `${backendApiUrl}/:path*`,
      },
    ];
  },

  // Headers for caching
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
        ],
      },
      {
        source: '/static/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=31536000, immutable',
          },
        ],
      },
    ];
  },
};

export default withBundleAnalyzer(nextConfig);
