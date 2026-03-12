package model;

import java.time.LocalDate;

public class LeaveHistory {
    private Long id;
    private String action;
    private LocalDate actionDate;
    private String note;
    private Employee employee;
    private LeaveRequest leaveRequest;

    public LeaveHistory() {
    }

    public LeaveHistory(Long id, String action, LocalDate actionDate, String note, Employee employee,
                        LeaveRequest leaveRequest) {
        this.id = id;
        this.action = action;
        this.actionDate = actionDate;
        this.note = note;
        this.employee = employee;
        this.leaveRequest = leaveRequest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDate actionDate) {
        this.actionDate = actionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LeaveRequest getLeaveRequest() {
        return leaveRequest;
    }

    public void setLeaveRequest(LeaveRequest leaveRequest) {
        this.leaveRequest = leaveRequest;
    }

    @Override
    public String toString() {
        return "LeaveHistory{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", actionDate=" + actionDate +
                ", note='" + note + '\'' +
                '}';
    }
}
