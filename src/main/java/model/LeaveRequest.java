package model;

import enums.LeaveRequestStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveRequest {
    private Long id;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveRequestStatus status;
    private String rejectionComment;
    private Employee employee;
    private LeaveType leaveType;
    private Admin processedBy;

    public LeaveRequest() {
    }

    public LeaveRequest(Long id, LocalDate requestDate, LocalDate startDate, LocalDate endDate, String reason,
                        LeaveRequestStatus status, String rejectionComment, Employee employee, LeaveType leaveType,
                        Admin processedBy) {
        this.id = id;
        this.requestDate = requestDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.rejectionComment = rejectionComment;
        this.employee = employee;
        this.leaveType = leaveType;
        this.processedBy = processedBy;
    }

    public void createRequest() {
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }
        if (status == null) {
            status = LeaveRequestStatus.PENDING;
        }
        if (reason != null) {
            reason = reason.trim();
        }
    }

    public void approve() {
        this.status = LeaveRequestStatus.APPROVED;
        this.rejectionComment = null;
    }

    public void reject(String comment) {
        this.status = LeaveRequestStatus.REJECTED;
        this.rejectionComment = comment;
    }

    public long calculateDuration() {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveRequestStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveRequestStatus status) {
        this.status = status;
    }

    public String getRejectionComment() {
        return rejectionComment;
    }

    public void setRejectionComment(String rejectionComment) {
        this.rejectionComment = rejectionComment;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Admin getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Admin processedBy) {
        this.processedBy = processedBy;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id=" + id +
                ", requestDate=" + requestDate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", rejectionComment='" + rejectionComment + '\'' +
                '}';
    }
}
