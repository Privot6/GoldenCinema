package com.goldencinema.backend.service;

import com.goldencinema.backend.dto.WeeklyProfitRow;
import com.goldencinema.backend.entity.ReservationStatus;
import com.goldencinema.backend.repository.ReservationRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReservationRepository reservationRepository;
    private volatile JasperReport compiledReport;

    public ReportService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    private JasperReport getCompiledReport() throws JRException {
        if (compiledReport == null) {
            synchronized (this) {
                if (compiledReport == null) {
                    InputStream is = getClass().getResourceAsStream("/reports/weekly_profit.jrxml");
                    compiledReport = JasperCompileManager.compileReport(is);
                }
            }
        }
        return compiledReport;
    }

    public byte[] generateWeeklyProfitPdf(LocalDate from, LocalDate to) {
        try {
            List<Object[]> raw = reservationRepository.getWeeklyProfitRawData(
                    ReservationStatus.POTWIERDZONA,
                    from.atStartOfDay(),
                    to.plusDays(1).atStartOfDay()
            );

            List<WeeklyProfitRow> rows = raw.stream()
                    .map(row -> new WeeklyProfitRow(
                            (String)     row[0],
                            (Long)       row[1],
                            (Long)       row[2],
                            row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO
                    ))
                    .toList();

            BigDecimal total = rows.stream()
                    .map(WeeklyProfitRow::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> params = new HashMap<>();
            params.put("REPORT_FROM", from.toString());
            params.put("REPORT_TO", to.toString());
            params.put("REPORT_TOTAL", total);

            JasperReport report = getCompiledReport();
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
            exporter.exportReport();

            return baos.toByteArray();
        } catch (JRException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd generowania raportu", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Błąd pobierania danych raportu", e);
        }
    }
}