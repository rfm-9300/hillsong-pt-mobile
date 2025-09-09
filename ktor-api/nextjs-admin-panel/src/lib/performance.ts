// Performance monitoring utilities

interface PerformanceMetrics {
  name: string;
  duration: number;
  timestamp: number;
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics[] = [];
  private observers: PerformanceObserver[] = [];

  constructor() {
    if (typeof window !== 'undefined') {
      this.initializeObservers();
    }
  }

  private initializeObservers() {
    // Observe navigation timing
    if ('PerformanceObserver' in window) {
      const navObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach((entry) => {
          this.recordMetric(entry.name, entry.duration);
        });
      });

      try {
        navObserver.observe({ entryTypes: ['navigation', 'measure'] });
        this.observers.push(navObserver);
      } catch (error) {
        console.warn('Performance observer not supported:', error);
      }
    }
  }

  // Record a custom performance metric
  recordMetric(name: string, duration: number) {
    const metric: PerformanceMetrics = {
      name,
      duration,
      timestamp: Date.now(),
    };

    this.metrics.push(metric);

    // Keep only last 100 metrics to prevent memory leaks
    if (this.metrics.length > 100) {
      this.metrics = this.metrics.slice(-100);
    }

    // Log slow operations
    if (duration > 1000) {
      console.warn(`Slow operation detected: ${name} took ${duration}ms`);
    }
  }

  // Measure function execution time
  async measureAsync<T>(name: string, fn: () => Promise<T>): Promise<T> {
    const start = performance.now();
    try {
      const result = await fn();
      const duration = performance.now() - start;
      this.recordMetric(name, duration);
      return result;
    } catch (error) {
      const duration = performance.now() - start;
      this.recordMetric(`${name} (error)`, duration);
      throw error;
    }
  }

  // Measure synchronous function execution time
  measure<T>(name: string, fn: () => T): T {
    const start = performance.now();
    try {
      const result = fn();
      const duration = performance.now() - start;
      this.recordMetric(name, duration);
      return result;
    } catch (error) {
      const duration = performance.now() - start;
      this.recordMetric(`${name} (error)`, duration);
      throw error;
    }
  }

  // Get performance metrics
  getMetrics(): PerformanceMetrics[] {
    return [...this.metrics];
  }

  // Get metrics by name pattern
  getMetricsByPattern(pattern: string): PerformanceMetrics[] {
    return this.metrics.filter(metric => metric.name.includes(pattern));
  }

  // Get average duration for a metric
  getAverageDuration(name: string): number {
    const matchingMetrics = this.metrics.filter(metric => metric.name === name);
    if (matchingMetrics.length === 0) return 0;
    
    const total = matchingMetrics.reduce((sum, metric) => sum + metric.duration, 0);
    return total / matchingMetrics.length;
  }

  // Clear all metrics
  clear() {
    this.metrics = [];
  }

  // Cleanup observers
  destroy() {
    this.observers.forEach(observer => observer.disconnect());
    this.observers = [];
    this.metrics = [];
  }
}

// Global performance monitor instance
export const performanceMonitor = new PerformanceMonitor();

// Utility functions
export const measureApiCall = async <T>(
  name: string,
  apiCall: () => Promise<T>
): Promise<T> => {
  return performanceMonitor.measureAsync(`API: ${name}`, apiCall);
};

export const measureComponentRender = <T>(
  componentName: string,
  renderFn: () => T
): T => {
  return performanceMonitor.measure(`Render: ${componentName}`, renderFn);
};

// Web Vitals monitoring
export const reportWebVitals = (metric: { name: string; value: number }) => {
  performanceMonitor.recordMetric(`WebVital: ${metric.name}`, metric.value);
  
  // Log important metrics
  if (['CLS', 'FID', 'FCP', 'LCP', 'TTFB'].includes(metric.name)) {
    console.log(`${metric.name}: ${metric.value}`);
  }
};

// Memory usage monitoring
export const monitorMemoryUsage = () => {
  if (typeof window !== 'undefined' && 'memory' in performance) {
    const memory = (performance as { memory: { usedJSHeapSize: number; totalJSHeapSize: number; jsHeapSizeLimit: number } }).memory;
    performanceMonitor.recordMetric('Memory: Used', memory.usedJSHeapSize);
    performanceMonitor.recordMetric('Memory: Total', memory.totalJSHeapSize);
    performanceMonitor.recordMetric('Memory: Limit', memory.jsHeapSizeLimit);
  }
};

// Bundle size monitoring
export const logBundleInfo = () => {
  if (typeof window !== 'undefined') {
    // Log initial bundle load time
    const navTiming = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
    if (navTiming) {
      performanceMonitor.recordMetric('Bundle: Load Time', navTiming.loadEventEnd - navTiming.fetchStart);
      performanceMonitor.recordMetric('Bundle: DOM Ready', navTiming.domContentLoadedEventEnd - navTiming.fetchStart);
    }
  }
};