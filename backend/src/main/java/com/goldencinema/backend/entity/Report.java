package com.goldencinema.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", length = 50, nullable = false)
    private String reportType;

    @ManyToOne
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @Column(name = "file_path", length = 255, nullable = false)
    private String filePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Report() {
    }

    public Long getId() {
        return id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public User getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(User generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
