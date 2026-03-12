package model;

import enums.EmployeeStatus;

public class Admin {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public Admin() {
    }

    public Admin(Long id, String firstName, String lastName, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public boolean login() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }

    public void addEmployee(Employee employee) {
        if (employee != null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }
    }

    public void updateEmployee(Employee employee) {
        if (employee != null) {
            employee.setStatus(employee.getStatus());
        }
    }

    public void deactivateEmployee(Employee employee) {
        if (employee != null) {
            employee.setStatus(EmployeeStatus.INACTIVE);
        }
    }

    public void approveLeaveRequest(LeaveRequest request) {
        if (request != null) {
            request.setProcessedBy(this);
            request.approve();
        }
    }

    public void rejectLeaveRequest(LeaveRequest request, String reason) {
        if (request != null) {
            request.setProcessedBy(this);
            request.reject(reason);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
