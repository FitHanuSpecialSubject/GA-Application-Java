package com.example.SS2_Backend.util;

import com.example.SS2_Backend.dto.response.ComputerSpecs;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@Slf4j
public class ComputerSpecsUtil {
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
