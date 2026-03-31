package model;

public class BookingDetail {
    private int id;
    private int bookingId;
    private int equipmentId;
    private int serviceId;
    private int quantity;

    public BookingDetail() {
    }

    public BookingDetail(int equipmentId, int serviceId, int quantity) {
        this.equipmentId = equipmentId;
        this.serviceId = serviceId;
        this.quantity = quantity;
    }

    public BookingDetail(int id, int bookingId, int equipmentId, int serviceId, int quantity) {
        this.id = id;
        this.bookingId = bookingId;
        this.equipmentId = equipmentId;
        this.serviceId = serviceId;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
