package model;

public class LeaveBalance {
    private Long id;
    private int year;
    private double earnedDays;
    private double usedDays;
    private double remainingDays;
    private Employee employee;

    public LeaveBalance() {
    }

    public LeaveBalance(Long id, int year, double earnedDays, double usedDays, double remainingDays, Employee employee) {
        this.id = id;
        this.year = year;
        this.earnedDays = earnedDays;
        this.usedDays = usedDays;
        this.remainingDays = remainingDays;
        this.employee = employee;
    }

    public double calculateBalance() {
        return earnedDays - usedDays;
    }

    public void updateBalance() {
        this.remainingDays = calculateBalance();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getEarnedDays() {
        return earnedDays;
    }

    public void setEarnedDays(double earnedDays) {
        this.earnedDays = earnedDays;
    }

    public double getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(double usedDays) {
        this.usedDays = usedDays;
    }

    public double getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(double remainingDays) {
        this.remainingDays = remainingDays;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "LeaveBalance{" +
                "id=" + id +
                ", year=" + year +
                ", earnedDays=" + earnedDays +
                ", usedDays=" + usedDays +
                ", remainingDays=" + remainingDays +
                '}';
    }
}
