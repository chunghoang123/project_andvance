package model;

public class Room {

    private int id;
    private String roomName;
    private int capacity;
    private String location;
    private int status; // 1: Active, 0: Maintenance

    public Room() {
    }

    public Room(int id, String roomName, int capacity, String location, int status) {
        this.id = id;
        this.roomName = roomName;
        this.capacity = capacity;
        this.location = location;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
