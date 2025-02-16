package org.fit.ssapp.util;

import lombok.extern.slf4j.Slf4j;
import org.fit.ssapp.dto.response.ComputerSpecs;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * Utility class for retrieving the computer's system specifications such as OS, CPU, and memory
 * information. Uses the OSHI library to gather the system details.
 */
@Slf4j
public class ComputerSpecsUtil {

  /**
   * Retrieves the system specifications including the operating system details, CPU information,
   * and memory. Uses the OSHI library to collect system data such as the operating system family,
   * manufacturer, version, CPU name, number of physical and logical cores, and total memory. If
   * there is an error during the process, default "Unknown" values will be returned for all specs.
   *
   * @return a {@link ComputerSpecs} object containing the system specifications
   */
  public static ComputerSpecs getComputerSpecs() {
    ComputerSpecs computerSpecs;
    try {
      SystemInfo systemInfo = new SystemInfo();
      HardwareAbstractionLayer hardware = systemInfo.getHardware();
      OperatingSystem operatingSystem = systemInfo.getOperatingSystem();

      // Operating System Information
      String osFamily = operatingSystem.getFamily();
      String osManufacturer = operatingSystem.getManufacturer();
      String osVersion = operatingSystem.getVersionInfo().getVersion();

      // CPU Information
      CentralProcessor processor = hardware.getProcessor();
      int cpuPhysicalCores = processor.getPhysicalProcessorCount();
      int cpuLogicalCores = processor.getLogicalProcessorCount();
      String cpuName = processor.getProcessorIdentifier().getName();

      // Memory Information
      GlobalMemory memory = hardware.getMemory();
      long totalMemory = memory.getTotal();

      computerSpecs = ComputerSpecs.builder()
          .osFamily(osFamily)
          .osManufacturer(osManufacturer)
          .osVersion(osVersion)
          .cpuName(cpuName)
          .cpuPhysicalCores(cpuPhysicalCores)
          .cpuLogicalCores(cpuLogicalCores)
          .totalMemory(FormatUtil.formatBytes(totalMemory))
          .build();

      return computerSpecs;

    } catch (Exception e) {
      log.error("Error while getting computer specs", e);
      computerSpecs = ComputerSpecs.builder()
          .osFamily("Unknown")
          .osManufacturer("Unknown")
          .osVersion("Unknown")
          .cpuName("Unknown")
          .cpuPhysicalCores(null)
          .cpuLogicalCores(null)
          .totalMemory("Unknown")
          .build();
    }
    return computerSpecs;
  }
}
