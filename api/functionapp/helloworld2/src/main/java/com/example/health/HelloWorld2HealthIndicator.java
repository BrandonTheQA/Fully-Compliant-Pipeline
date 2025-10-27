package com.example.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * Custom health indicator for HelloWorld2 function
 */
@Component
public class HelloWorld2HealthIndicator implements HealthIndicator {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String version;
    
    @Value("${app.build-date}")
    private String buildDate;
    
    @Override
    public Health health() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            long uptime = runtimeBean.getUptime();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long freeMemory = maxMemory - usedMemory;
            
            String uptimeFormatted = formatUptime(uptime);
            
            return Health.up()
                    .withDetail("app", appName)
                    .withDetail("version", version)
                    .withDetail("buildDate", buildDate)
                    .withDetail("uptime", uptimeFormatted)
                    .withDetail("memory", Map.of(
                        "used", usedMemory,
                        "free", freeMemory,
                        "max", maxMemory,
                        "usagePercent", (double) usedMemory / maxMemory * 100
                    ))
                    .withDetail("jvm", Map.of(
                        "name", runtimeBean.getVmName(),
                        "version", runtimeBean.getVmVersion(),
                        "vendor", runtimeBean.getVmVendor()
                    ))
                    .build();
                    
        } catch (Exception e) {
            return Health.down()
                    .withDetail("app", appName)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
