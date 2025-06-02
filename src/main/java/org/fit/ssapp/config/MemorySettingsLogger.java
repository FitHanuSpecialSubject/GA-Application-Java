package org.fit.ssapp.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class MemorySettingsLogger {

  private static final Logger logger = LoggerFactory.getLogger(MemorySettingsLogger.class);

  @PostConstruct
  @SuppressWarnings("RedundantAssignment")
  public void logMemorySettings() {
    long maxHeap     = Runtime.getRuntime().maxMemory();    // Xmx
    long totalHeap   = Runtime.getRuntime().totalMemory();  // current allocated
    long freeHeap    = Runtime.getRuntime().freeMemory();   // free in allocated
    long initialHeap = totalHeap;                           // Approximation of Xms

    logger.info("JVM Heap Settings:");
    logger.info("  -Xms (Initial heap size): {} MB", bytesToMb(initialHeap));
    logger.info("  -Xmx (Max heap size):     {} MB", bytesToMb(maxHeap));
    logger.info("  Currently allocated:      {} MB", bytesToMb(totalHeap));
    logger.info("  Free memory in heap:      {} MB", bytesToMb(freeHeap));
  }

  private long bytesToMb(long bytes) {
    return bytes / (1024 * 1024);
  }
}
