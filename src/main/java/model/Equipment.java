package model;

public class Equipment {
    private int id;
    private String name;
    private int totalQty;
    private int availableQty;

    public Equipment() {
    }

    public Equipment(int id, String name, int totalQty, int availableQty) {
        this.id = id;
        this.name = name;
        this.totalQty = totalQty;
        this.availableQty = availableQty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(int availableQty) {
        this.availableQty = availableQty;
    }
}
