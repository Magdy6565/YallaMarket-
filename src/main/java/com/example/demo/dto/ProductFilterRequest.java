package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterRequest {

    private Integer quantity; // Filter for quantity greater than or equal to
    private String category;

}