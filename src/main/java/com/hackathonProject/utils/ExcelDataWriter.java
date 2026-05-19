package com.hackathonProject.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hackathonProject.pages.SearchResultsPage.CourseInfo;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelDataWriter {

    private static final Logger logger = LogManager.getLogger(ExcelDataWriter.class);

    public static void writeCourseData(List<CourseInfo> courses, String filePath) {
        logger.info("Writing " + courses.size() + " courses to Excel: " + filePath);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("Web Dev Courses");

            // Timestamp row
            Row tsRow = sheet.createRow(0);
            tsRow.createCell(0).setCellValue("Extracted: " +
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));

            // Header row
            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("S.No");
            headerRow.createCell(1).setCellValue("Course Name");
            headerRow.createCell(2).setCellValue("Learning Hours");
            headerRow.createCell(3).setCellValue("Rating");

            // Data rows
            for (int i = 0; i < courses.size(); i++) {
                CourseInfo course = courses.get(i);
                Row row = sheet.createRow(i + 2);
                row.createCell(0).setCellValue(course.serialNo);
                row.createCell(1).setCellValue(course.name);
                row.createCell(2).setCellValue(course.hours);
                row.createCell(3).setCellValue(course.rating);
            }

            // Auto-size columns
            for (int col = 0; col < 4; col++) {
                sheet.autoSizeColumn(col);
            }

            // Save
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                logger.info("Excel file saved: " + filePath);
            }

        } catch (IOException e) {
            logger.error("Failed to write Excel file: " + e.getMessage());
            throw new RuntimeException("Excel write error: " + e.getMessage());
        }
    }

    public static String[][] readTestData(String filePath, String sheetName) {
        logger.info("Reading test data from: " + filePath + " | Sheet: " + sheetName);
        try (XSSFWorkbook wb = new XSSFWorkbook(new java.io.FileInputStream(filePath))) {
            XSSFSheet sheet = wb.getSheet(sheetName);
            int rows = sheet.getLastRowNum();
            int cols = sheet.getRow(0).getLastCellNum();
            String[][] data = new String[rows][cols];
            for (int r = 1; r <= rows; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (int c = 0; c < cols; c++) {
                    Cell cell = row.getCell(c);
                    data[r - 1][c] = (cell != null) ? cell.toString() : "";
                }
            }
            return data;
        } catch (Exception e) {
            logger.error("Excel read error: " + e.getMessage());
            return new String[0][0];
        }
    }
}