package model;

public class Booking {
    private int id;
    private int userId;
    private int roomId;
    private String startTime;
    private String endTime;
    private String status;
    private String prepStatus;
    private int supportId;

    public Booking() {
    }

    public Booking(int userId, int roomId, String startTime, String endTime) {
        this.userId = userId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "pending";
        this.prepStatus = "not_prepared";
        this.supportId = 0;
    }

    public Booking(int id, int userId, int roomId, String startTime, String endTime, String status, String prepStatus, int supportId) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.prepStatus = prepStatus;
        this.supportId = supportId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrepStatus() {
        return prepStatus;
    }

    public void setPrepStatus(String prepStatus) {
        this.prepStatus = prepStatus;
    }

    public int getSupportId() {
        return supportId;
    }

    public void setSupportId(int supportId) {
        this.supportId = supportId;
    }
}
