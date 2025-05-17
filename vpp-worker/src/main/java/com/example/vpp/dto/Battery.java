package com.example.vpp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Battery implements Serializable {

    private String name;
    private Integer postcode;
    private Double capacity;
}
