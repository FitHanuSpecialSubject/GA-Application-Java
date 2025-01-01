package com.example.SS2_Backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
