package com.tka.Entity;


import lombok.Data;

@Data
public class ExcelError{

    private int row;

    private String field;

    private String message;

    public ExcelError(int row, String field, String message) {
        this.row = row;
        this.field = field;
        this.message = message;
    }
}