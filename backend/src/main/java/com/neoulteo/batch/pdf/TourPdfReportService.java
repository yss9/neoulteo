package com.neoulteo.batch.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.neoulteo.batch.dto.TourAttractionDto;
import com.neoulteo.batch.dto.TourAttractionChangeDto;
import com.neoulteo.batch.dto.TourBatchComparisonResult;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TourPdfReportService {
    @Value("${tour.batch.pdf-output-dir:output}")
    private String outputDir;

    public String createReport(TourBatchComparisonResult comparison, String summary) throws Exception {
        String reportDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outFile = new File(dir, "tour-report-" + reportDate + ".pdf");
        String html = buildHtml(reportDate, comparison, summary);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            outFile = timestampedFile(dir, reportDate);
            outputStream = new FileOutputStream(outFile);
        }

        try (FileOutputStream fos = outputStream) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, new File(".").toURI().toString());
            builder.toStream(fos);

            File malgun = new File("C:/Windows/Fonts/malgun.ttf");
            if (malgun.exists()) {
                builder.useFont(malgun, "MalgunGothic");
            }
            builder.run();
        }

        return outFile.getPath();
    }

    private File timestampedFile(File dir, String reportDate) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return new File(dir, "tour-report-" + reportDate + "-" + timestamp + ".pdf");
    }

    private String buildHtml(String reportDate, TourBatchComparisonResult comparison, String summary) {
        TourBatchComparisonResult result = comparison == null ? new TourBatchComparisonResult() : comparison;
        StringBuilder sb = new StringBuilder(30_000);
        sb.append("<!DOCTYPE html>");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\" />");
        sb.append("<title>Neoulteo Batch Tour Report</title>");
        sb.append("<style>");
        sb.append("@page { size: A4; margin: 18mm 16mm; }");
        sb.append("body { font-family: MalgunGothic, Arial, sans-serif; color: #202124; font-size: 12px; }");
        sb.append("h1 { font-size: 22px; margin: 0 0 6px; }");
        sb.append("h2 { font-size: 15px; margin: 20px 0 8px; border-bottom: 1px solid #ddd; padding-bottom: 6px; }");
        sb.append(".meta { color: #666; margin-bottom: 16px; }");
        sb.append(".summary { white-space: pre-wrap; line-height: 1.65; border: 1px solid #e5e7eb; padding: 12px; }");
        sb.append(".process { color: #444; line-height: 1.6; }");
        sb.append(".stats { display: table; width: 100%; margin: 12px 0 8px; border-spacing: 8px 0; }");
        sb.append(".stat { display: table-cell; border: 1px solid #d9e2ec; padding: 10px; }");
        sb.append(".stat strong { display: block; font-size: 18px; color: #0f766e; margin-bottom: 4px; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 8px; table-layout: fixed; }");
        sb.append("th { background: #f8f9fa; text-align: left; }");
        sb.append("th, td { border-bottom: 1px solid #eceff1; padding: 7px; vertical-align: top; word-wrap: break-word; }");
        sb.append(".muted { color: #777; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>Neoulteo \uAD00\uAD11\uC9C0 \uBC30\uCE58 \uB9AC\uD3EC\uD2B8</h1>");
        sb.append("<div class=\"meta\">\uC0DD\uC131\uC77C: ").append(esc(reportDate))
                .append(" / TourAPI \uC218\uC9D1: ").append(result.getApiItemCount()).append("\uAC74</div>");

        sb.append("<h2>\uBC30\uCE58 \uCC98\uB9AC \uB0B4\uC6A9</h2>");
        sb.append("<div class=\"process\">")
                .append("\uD55C\uAD6D\uAD00\uAD11\uACF5\uC0AC TourAPI\uC5D0\uC11C \uAD00\uAD11\uC9C0 \uB370\uC774\uD130\uB97C \uC218\uC9D1\uD55C \uB4A4, ")
                .append("\uD604\uC7AC DB\uC758 \uAD00\uAD11\uC9C0\uC640 contentId \uAE30\uC900\uC73C\uB85C \uBE44\uAD50\uD574 ")
                .append("\uC2E0\uADDC, \uBCC0\uACBD, \uC0AD\uC81C \uD6C4\uBCF4\uB97C \uACC4\uC0B0\uD588\uC2B5\uB2C8\uB2E4. ")
                .append("\uBE44\uAD50 \uACB0\uACFC\uB294 DB \uB9AC\uD3EC\uD2B8\uC640 PDF\uC5D0 \uC800\uC7A5\uB429\uB2C8\uB2E4.")
                .append("</div>");

        sb.append("<h2>\uBE44\uAD50 \uC694\uC57D</h2>");
        sb.append("<div class=\"stats\">");
        appendStat(sb, "\uC218\uC9D1", result.getApiItemCount());
        appendStat(sb, "\uC2E0\uADDC", result.getNewItemCount());
        appendStat(sb, "\uBCC0\uACBD", result.getChangedItemCount());
        appendStat(sb, "\uC0AD\uC81C \uD6C4\uBCF4", result.getMissingItemCount());
        appendStat(sb, "\uB3D9\uC77C", result.getUnchangedItemCount());
        sb.append("</div>");

        appendContentTypeSummary(sb, result);

        sb.append("<h2>AI \uCD94\uCC9C \uC694\uC57D</h2>");
        sb.append("<div class=\"summary\">").append(esc(summary)).append("</div>");

        appendChangeSection(sb, "\uC2E0\uADDC \uAD00\uAD11\uC9C0", result.getNewItems());
        appendChangeSection(sb, "\uBCC0\uACBD \uAD00\uAD11\uC9C0", result.getChangedItems());
        appendChangeSection(sb, "\uC0AD\uC81C \uD6C4\uBCF4", result.getMissingItems());

        sb.append("</body></html>");
        return sb.toString();
    }

    private void appendContentTypeSummary(StringBuilder sb, TourBatchComparisonResult result) {
        Map<String, TypeStats> stats = new LinkedHashMap<>();
        addApiCounts(stats, result.getApiItems());
        addChangeCounts(stats, result.getNewItems(), "new");
        addChangeCounts(stats, result.getChangedItems(), "changed");
        addChangeCounts(stats, result.getMissingItems(), "missing");
        addChangeCounts(stats, result.getUnchangedItems(), "unchanged");

        sb.append("<h2>\uCF58\uD150\uCE20 \uD0C0\uC785\uBCC4 \uC694\uC57D</h2>");
        if (stats.isEmpty()) {
            sb.append("<div class=\"muted\">\uCF58\uD150\uCE20 \uD0C0\uC785\uBCC4 \uC9D1\uACC4\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.</div>");
            return;
        }

        sb.append("<table>");
        sb.append("<tr><th>\uD0C0\uC785</th><th>\uC218\uC9D1</th><th>\uC2E0\uADDC</th><th>\uBCC0\uACBD</th><th>\uC0AD\uC81C \uD6C4\uBCF4</th><th>\uB3D9\uC77C</th></tr>");
        for (Map.Entry<String, TypeStats> entry : stats.entrySet()) {
            TypeStats value = entry.getValue();
            sb.append("<tr>");
            sb.append("<td>").append(esc(typeLabel(entry.getKey()))).append("</td>");
            sb.append("<td>").append(value.api).append("</td>");
            sb.append("<td>").append(value.newItems).append("</td>");
            sb.append("<td>").append(value.changed).append("</td>");
            sb.append("<td>").append(value.missing).append("</td>");
            sb.append("<td>").append(value.unchanged).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
    }

    private void addApiCounts(Map<String, TypeStats> stats, List<TourAttractionDto> items) {
        if (items == null) {
            return;
        }
        for (TourAttractionDto item : items) {
            stats.computeIfAbsent(typeKey(item.getContentTypeId()), key -> new TypeStats()).api++;
        }
    }

    private void addChangeCounts(Map<String, TypeStats> stats, List<TourAttractionChangeDto> items, String field) {
        if (items == null) {
            return;
        }
        for (TourAttractionChangeDto item : items) {
            TypeStats value = stats.computeIfAbsent(typeKey(item.getContentTypeId()), key -> new TypeStats());
            switch (field) {
                case "new" -> value.newItems++;
                case "changed" -> value.changed++;
                case "missing" -> value.missing++;
                case "unchanged" -> value.unchanged++;
                default -> {
                }
            }
        }
    }

    private void appendStat(StringBuilder sb, String label, int count) {
        sb.append("<div class=\"stat\"><strong>")
                .append(count)
                .append("</strong>")
                .append(esc(label))
                .append("</div>");
    }

    private void appendChangeSection(StringBuilder sb, String title, List<TourAttractionChangeDto> items) {
        sb.append("<h2>").append(esc(title)).append("</h2>");
        if (items == null || items.isEmpty()) {
            sb.append("<div class=\"muted\">\uD574\uB2F9 \uB370\uC774\uD130\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.</div>");
            return;
        }

        sb.append("<table>");
        sb.append("<tr><th>\uCF58\uD150\uCE20 ID</th><th>\uAD00\uAD11\uC9C0\uBA85</th><th>\uC0C1\uC138</th></tr>");
        int limit = Math.min(items.size(), 40);
        for (int i = 0; i < limit; i++) {
            TourAttractionChangeDto item = items.get(i);
            sb.append("<tr>");
            sb.append("<td>").append(esc(item.getContentId())).append("</td>");
            sb.append("<td>").append(esc(item.getTitle())).append("</td>");
            sb.append("<td>").append(esc(item.getChangeDescription())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        if (items.size() > limit) {
            sb.append("<div class=\"muted\">\uC678 ")
                    .append(items.size() - limit)
                    .append("\uAC74\uC740 PDF \uD45C\uC2DC \uAC1C\uC218 \uC81C\uD55C\uC73C\uB85C \uC0DD\uB7B5\uD588\uC2B5\uB2C8\uB2E4.</div>");
        }
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String typeKey(String contentTypeId) {
        return contentTypeId == null || contentTypeId.isBlank() ? "-" : contentTypeId.trim();
    }

    private String typeLabel(String contentTypeId) {
        return switch (typeKey(contentTypeId)) {
            case "12" -> "12 \uAD00\uAD11\uC9C0";
            case "14" -> "14 \uBB38\uD654\uC2DC\uC124";
            case "15" -> "15 \uD589\uC0AC/\uACF5\uC5F0/\uCD95\uC81C";
            case "25" -> "25 \uC5EC\uD589\uCF54\uC2A4";
            case "28" -> "28 \uB808\uD3EC\uCE20";
            case "32" -> "32 \uC219\uBC15";
            case "38" -> "38 \uC1FC\uD551";
            case "39" -> "39 \uC74C\uC2DD\uC810";
            default -> contentTypeId;
        };
    }

    private static class TypeStats {
        private int api;
        private int newItems;
        private int changed;
        private int missing;
        private int unchanged;
    }
}
