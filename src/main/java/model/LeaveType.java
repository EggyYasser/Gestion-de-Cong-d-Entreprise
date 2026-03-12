package model;

public class LeaveType {
    private Long id;
    private String name;
    private String description;
    private int maxDays;

    public LeaveType() {
    }

    public LeaveType(Long id, String name, String description, int maxDays) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxDays = maxDays;
    }

    public void createLeaveType() {
        normalizeData();
    }

    public void updateLeaveType() {
        normalizeData();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(int maxDays) {
        this.maxDays = maxDays;
    }

    private void normalizeData() {
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
        if (maxDays < 0) {
            maxDays = 0;
        }
    }

    @Override
    public String toString() {
        return "LeaveType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", maxDays=" + maxDays +
                '}';
    }
}
