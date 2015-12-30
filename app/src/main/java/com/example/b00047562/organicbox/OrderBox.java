package com.example.b00047562.organicbox;

/**
 * Created by Administrator on 12/20/2015.
 */
public class OrderBox {
    private String name;
    private String ordernum;
    private String date;
    private String image;
    private String type;
    private String stat;
    private String price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrdernum() {
        return ordernum;
    }

    public void setOrdernum(String ordernum) {
        this.ordernum = ordernum;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;

    }
    public void setType(String type)
    {
        this.type=type;
    }
    public String getType() {
        return type;
    }
    public void setStatus(String stat)
    {
        this.stat=stat;
    }
    public String getStatus()
    {
        return stat;
    }

    public void setPrice(String price)
    {
        this.price=price;
    }
    public String getPrice()
    {
        return price;
    }
}
