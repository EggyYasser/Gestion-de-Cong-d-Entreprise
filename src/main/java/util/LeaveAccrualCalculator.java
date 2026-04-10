package util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Algerian-style annual leave accrual: 30 days per full year, 2.5 days per calendar month worked
 * (30 ÷ 12), capped at 30 days per civil year.
 */
public final class LeaveAccrualCalculator {

    public static final double DAYS_PER_MONTH = 2.5;
    public static final double DAYS_PER_YEAR = 30.0;

    private LeaveAccrualCalculator() {
    }

    /**
     * Earned entitlement for civil year {@code year}, from hire date through {@code asOfDate} (inclusive),
     * counting each calendar month that overlaps the employment segment as one accrual month.
     */
    public static double earnedDaysForYear(LocalDate hireDate, int year, LocalDate asOfDate) {
        if (hireDate == null || asOfDate == null) {
            return 0.0;
        }
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        if (hireDate.isAfter(yearEnd)) {
            return 0.0;
        }
        LocalDate periodStart = hireDate.isAfter(yearStart) ? hireDate : yearStart;
        LocalDate periodEnd = yearEnd.isBefore(asOfDate) ? yearEnd : asOfDate;
        if (periodStart.isAfter(periodEnd)) {
            return 0.0;
        }
        YearMonth ymStart = YearMonth.from(periodStart);
        YearMonth ymEnd = YearMonth.from(periodEnd);
        long months = ymStart.until(ymEnd, ChronoUnit.MONTHS) + 1;
        double raw = months * DAYS_PER_MONTH;
        return round2(Math.min(DAYS_PER_YEAR, raw));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
