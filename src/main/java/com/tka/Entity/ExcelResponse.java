package com.tka.Entity;

import java.util.List;

import lombok.Data;

@Data
public class ExcelResponse {

    private int totalRows;

    private int successRows;

    private int failedRows;

    private List<ExcelError> errors;

    public ExcelResponse(int totalRows,
                         int successRows,
                         int failedRows,
                         List<ExcelError> errors) {

        this.totalRows = totalRows;
        this.successRows = successRows;
        this.failedRows = failedRows;
        this.errors = errors;
    }
}