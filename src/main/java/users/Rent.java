package users;

import java.util.Date;

public class Rent {
    private String bookName;
    private String renterName;
    private Date startDate;
    private Date endDate;
    public Rent() {}
    public Rent(String bookName, String renterName, Date startDate, Date endDate) {
        this.bookName = bookName;
        this.renterName = renterName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
