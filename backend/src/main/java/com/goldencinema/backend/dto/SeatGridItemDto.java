package com.goldencinema.backend.dto;

public class SeatGridItemDto {

    private Integer gridRow;
    private Integer gridCol;
    private String rowLabel;
    private Integer seatNumber;

    public SeatGridItemDto() {}

    public SeatGridItemDto(Integer gridRow, Integer gridCol, String rowLabel, Integer seatNumber) {
        this.gridRow = gridRow;
        this.gridCol = gridCol;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
    }

    public Integer getGridRow() { return gridRow; }
    public void setGridRow(Integer gridRow) { this.gridRow = gridRow; }

    public Integer getGridCol() { return gridCol; }
    public void setGridCol(Integer gridCol) { this.gridCol = gridCol; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }
}
