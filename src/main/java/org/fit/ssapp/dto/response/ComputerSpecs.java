package org.fit.ssapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hardware details of the machine running the algorithm.
 * **Main Attributes:**
 * Operating System:**
 * `osFamily` → The general OS category (e.g., "Windows", "Linux", "Mac").
 * `osManufacturer` → The OS provider (e.g., "Microsoft", "Canonical").
 * `osVersion` → The specific version of the OS.
 *  *
 * CPU Information:**
 * `cpuName` → The processor model (e.g., "Intel Core i7-12700K").
 * `cpuPhysicalCores` → The number of **physical** CPU cores.
 * `cpuLogicalCores` → The number of **logical** CPU cores (including hyper-threading).
 *  *
 * Memory Information:**
 * `totalMemory` → The total available system memory (e.g., "32 GB RAM").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComputerSpecs {

  String osFamily;
  String osManufacturer;
  String osVersion;
  String cpuName;
  Integer cpuPhysicalCores;
  Integer cpuLogicalCores;
  String totalMemory;

}
