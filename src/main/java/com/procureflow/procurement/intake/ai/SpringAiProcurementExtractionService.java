package com.procureflow.procurement.intake.ai;

import com.procureflow.procurement.intake.ProcurementCategory;
import com.procureflow.procurement.intake.dto.ProcurementExtraction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A local, key-free extractor that supports a usable first run without an AI account. */
@Service
public class SpringAiProcurementExtractionService implements ProcurementExtractionService {
    private static final Pattern QUANTITY = Pattern.compile("(?i)\\b(?:qty|quantity|of)\\s*[:=]?\\s*(\\d+)\\b|\\b(\\d+)\\s+(?:units|items|laptops|licenses|licences|desks|chairs|monitors)\\b");
    private static final Pattern MONEY = Pattern.compile("(?i)(?:₹|\\bINR\\b|\\bUSD\\b|\\bEUR\\b|\\$|€)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)|([0-9][0-9,]*(?:\\.[0-9]{1,2})?)\\s*(?:INR|USD|EUR|rupees?|dollars?|euros?)\\b");
    private static final Pattern DATE = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b|(?i)\\bwithin\\s+(\\d+)\\s+days?\\b");

    @Override
    public ProcurementExtraction extract(String rawRequest) {
        String text = rawRequest == null ? "" : rawRequest.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        Integer quantity = findInteger(QUANTITY, text);
        Matcher money = MONEY.matcher(text);
        BigDecimal budget = money.find() ? decimal(money.group(1) != null ? money.group(1) : money.group(2)) : null;
        String currency = lower.contains("$") || lower.contains("usd") || lower.contains("dollar") ? "USD"
                : lower.contains("€") || lower.contains("eur") || lower.contains("euro") ? "EUR"
                : lower.contains("₹") || lower.contains("inr") || lower.contains("rupee") ? "INR" : null;
        LocalDate deadline = null;
        Matcher date = DATE.matcher(text);
        if (date.find()) {
            try {
                deadline = date.group(1) != null ? LocalDate.parse(date.group(1)) : LocalDate.now().plusDays(Long.parseLong(date.group(2)));
            } catch (RuntimeException ignored) { /* Keep the field empty so the user can clarify. */ }
        }
        ProcurementCategory category = category(lower);
        String description = text.length() > 500 ? text.substring(0, 500) : text;
        return new ProcurementExtraction(description, quantity, budget, currency, deadline, category);
    }

    private static Integer findInteger(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) return null;
        try { return Integer.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2)); }
        catch (RuntimeException ignored) { return null; }
    }

    private static BigDecimal decimal(String value) {
        try { return new BigDecimal(value.replace(",", "")); }
        catch (RuntimeException ignored) { return null; }
    }

    private static ProcurementCategory category(String text) {
        if (contains(text, "laptop", "computer", "hardware", "monitor", "server")) return ProcurementCategory.IT_HARDWARE;
        if (contains(text, "software", "license", "licence", "saas", "subscription")) return ProcurementCategory.SOFTWARE;
        if (contains(text, "office supply", "stationery", "paper", "pen")) return ProcurementCategory.OFFICE_SUPPLIES;
        if (contains(text, "consult", "professional service", "legal service")) return ProcurementCategory.PROFESSIONAL_SERVICES;
        if (contains(text, "marketing", "advertising", "campaign")) return ProcurementCategory.MARKETING;
        if (contains(text, "facility", "facilities", "cleaning", "maintenance")) return ProcurementCategory.FACILITIES;
        if (contains(text, "travel", "flight", "hotel")) return ProcurementCategory.TRAVEL;
        if (contains(text, "logistics", "shipping", "freight", "delivery")) return ProcurementCategory.LOGISTICS;
        if (contains(text, "raw material", "steel", "plastic", "component")) return ProcurementCategory.RAW_MATERIALS;
        if (contains(text, "chair", "desk", "furniture")) return ProcurementCategory.OFFICE_SUPPLIES;
        return null;
    }

    private static boolean contains(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }
}
