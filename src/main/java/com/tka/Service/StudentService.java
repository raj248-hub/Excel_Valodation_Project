package com.tka.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tka.Entity.ExcelError;
import com.tka.Entity.ExcelResponse;
import com.tka.Entity.Students;
import com.tka.repository.ExcelRepository;

@Service
public class StudentService {

    @Autowired
    private ExcelRepository repository;

    public ExcelResponse uploadExcel(MultipartFile file) {

        List<ExcelError> errors = new ArrayList<>();

        int totalRows = 0;
        int successRows = 0;
        int failedRows = 0;

        // =========================================
        // 1. FILE VALIDATION
        // =========================================

        // File missing
        if (file == null) {

            errors.add(
                new ExcelError(
                    0,
                    "File",
                    "File is missing"
                )
            );

            return new ExcelResponse(0, 0, 0, errors);
        }

        // File empty
        if (file.isEmpty()) {

            errors.add(
                new ExcelError(
                    0,
                    "File",
                    "File is empty"
                )
            );

            return new ExcelResponse(0, 0, 0, errors);
        }

        // Check file name
        String fileName = file.getOriginalFilename();

        if (fileName == null ||
            !fileName.toLowerCase().endsWith(".xlsx")) {

            errors.add(
                new ExcelError(
                    0,
                    "File",
                    "Only .xlsx files are allowed"
                )
            );

            return new ExcelResponse(0, 0, 0, errors);
        }

        try {

            // =========================================
            // 2. OPEN EXCEL
            // =========================================

            Workbook workbook =
                new XSSFWorkbook(file.getInputStream());

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();

            // =========================================
            // 3. CHECK EXCEL HAS DATA
            // =========================================

            if (sheet.getPhysicalNumberOfRows() <= 1) {

                errors.add(
                    new ExcelError(
                        0,
                        "File",
                        "Excel file must contain header and at least one data row"
                    )
                );

                workbook.close();

                return new ExcelResponse(
                    0,
                    0,
                    0,
                    errors
                );
            }

            // =========================================
            // 4. HEADER VALIDATION
            // =========================================

            Row header = sheet.getRow(0);

            if (header == null) {

                errors.add(
                    new ExcelError(
                        1,
                        "Header",
                        "Excel header is missing"
                    )
                );

                workbook.close();

                return new ExcelResponse(
                    0,
                    0,
                    0,
                    errors
                );
            }

            String[] expectedHeaders = {
                "student_name",
                "email",
                "mobile",
                "course",
                "city",
                "fees"
            };

            // Check each header
            for (int i = 0; i < expectedHeaders.length; i++) {

                String actualHeader =
                    formatter.formatCellValue(header.getCell(i)).trim();

                if (!expectedHeaders[i]
                        .equalsIgnoreCase(actualHeader)) {

                    errors.add(
                        new ExcelError(
                            1,
                            expectedHeaders[i],
                            "Invalid column name. Expected: "
                            + expectedHeaders[i]
                        )
                    );
                }
            }

            // If header is wrong, stop
            if (!errors.isEmpty()) {

                workbook.close();

                return new ExcelResponse(
                    0,
                    0,
                    0,
                    errors
                );
            }

            // =========================================
            // 5. DUPLICATE CHECK INSIDE EXCEL
            // =========================================

            Set<String> excelEmails = new HashSet<>();
            Set<String> excelMobiles = new HashSet<>();

            // =========================================
            // 6. READ EVERY ROW
            // =========================================

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                int excelRowNumber = i + 1;

                totalRows++;

                boolean rowValid = true;

                // =====================================
                // READ VALUES
                // =====================================

                String studentName =
                    formatter.formatCellValue(
                        row.getCell(0)
                    ).trim();

                String email =
                    formatter.formatCellValue(
                        row.getCell(1)
                    ).trim();

                String mobile =
                    formatter.formatCellValue(
                        row.getCell(2)
                    ).trim();

                String course =
                    formatter.formatCellValue(
                        row.getCell(3)
                    ).trim();

                String city =
                    formatter.formatCellValue(
                        row.getCell(4)
                    ).trim();

                String feesText =
                    formatter.formatCellValue(
                        row.getCell(5)
                    ).trim();

                // =====================================
                // STUDENT NAME VALIDATION
                // =====================================

                if (studentName.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "student_name",
                            "Student name is required"
                        )
                    );

                    rowValid = false;

                } else if (studentName.length() < 3) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "student_name",
                            "Student name must contain at least 3 characters"
                        )
                    );

                    rowValid = false;

                } else if (!studentName.matches(
                        "[A-Za-z]+( [A-Za-z]+)*")) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "student_name",
                            "Student name must contain only characters and single spaces"
                        )
                    );

                    rowValid = false;
                }

                // =====================================
                // EMAIL VALIDATION
                // =====================================

                if (email.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "email",
                            "Email is required"
                        )
                    );

                    rowValid = false;

                } else if (!Pattern.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
                        email)) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "email",
                            "Invalid email format"
                        )
                    );

                    rowValid = false;

                } else if (repository.existsByEmail(email)) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "email",
                            "Email already exists in database"
                        )
                    );

                    rowValid = false;

                } else if (!excelEmails.add(email.toLowerCase())) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "email",
                            "Duplicate email in Excel file"
                        )
                    );

                    rowValid = false;
                }

                // =====================================
                // MOBILE VALIDATION
                // =====================================

                if (mobile.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "mobile",
                            "Mobile is required"
                        )
                    );

                    rowValid = false;

                } else if (!mobile.matches("\\d{10}")) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "mobile",
                            "Mobile must contain exactly 10 digits"
                        )
                    );

                    rowValid = false;

                } else if (repository.existsByMobile(mobile)) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "mobile",
                            "Mobile already exists in database"
                        )
                    );

                    rowValid = false;

                } else if (!excelMobiles.add(mobile)) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "mobile",
                            "Duplicate mobile number in Excel file"
                        )
                    );

                    rowValid = false;
                }

                // =====================================
                // COURSE VALIDATION
                // =====================================

                if (course.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "course",
                            "Course is required"
                        )
                    );

                    rowValid = false;

                } else if (!course.equalsIgnoreCase("Java")
                        && !course.equalsIgnoreCase("Python")
                        && !course.equalsIgnoreCase("Testing")
                        && !course.equalsIgnoreCase("Data Analytics")) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "course",
                            "Course must be Java, Python, Testing or Data Analytics"
                        )
                    );

                    rowValid = false;

                } else {

                    // Standardize course name

                    if (course.equalsIgnoreCase("java")) {
                        course = "Java";
                    }

                    else if (course.equalsIgnoreCase("python")) {
                        course = "Python";
                    }

                    else if (course.equalsIgnoreCase("testing")) {
                        course = "Testing";
                    }

                    else if (course.equalsIgnoreCase("data analytics")) {
                        course = "Data Analytics";
                    }
                }

                // =====================================
                // CITY VALIDATION
                // =====================================

                if (city.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "city",
                            "City cannot be empty or contain only spaces"
                        )
                    );

                    rowValid = false;

                } else if (!city.matches(
                        "[A-Za-z]+( [A-Za-z]+)*")) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "city",
                            "City must contain only characters and single spaces"
                        )
                    );

                    rowValid = false;
                }

                // =====================================
                // FEES VALIDATION
                // =====================================

                double fees = 0;

                if (feesText.isEmpty()) {

                    errors.add(
                        new ExcelError(
                            excelRowNumber,
                            "fees",
                            "Fees is required"
                        )
                    );

                    rowValid = false;

                } else {

                    try {

                        fees = Double.parseDouble(feesText);

                        if (fees <= 0) {

                            errors.add(
                                new ExcelError(
                                    excelRowNumber,
                                    "fees",
                                    "Fees must be greater than 0"
                                )
                            );

                            rowValid = false;
                        }

                    } catch (NumberFormatException e) {

                        errors.add(
                            new ExcelError(
                                excelRowNumber,
                                "fees",
                                "Fees must be a valid number"
                            )
                        );

                        rowValid = false;
                    }
                }

                // =====================================
                // SAVE VALID ROW
                // =====================================

                if (rowValid) {

                    Students student = new Students();

                    student.setStudent_name(studentName);
                    student.setEmail(email);
                    student.setMobile(mobile);
                    student.setCourse(course);
                    student.setCity(city);
                    student.setFees(fees);

                    repository.save(student);

                    successRows++;

                } else {

                    failedRows++;
                }
            }

            workbook.close();

        } catch (Exception e) {

            errors.add(
                new ExcelError(
                    0,
                    "File",
                    "Invalid or corrupted XLSX file"
                )
            );
        }

        // =========================================
        // FINAL RESPONSE
        // =========================================

        return new ExcelResponse(
            totalRows,
            successRows,
            failedRows,
            errors
        );
    }
}