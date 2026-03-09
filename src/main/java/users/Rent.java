package users;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Rent {
    private String bookName;
    private String renterName;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private String startDate;
    private String endDate;
    public Rent() {}
    public Rent(String bookName, String renterName, Date startDate, Date endDate) {
        this.bookName = bookName;
        this.renterName = renterName;
        this.startDate = format.format(startDate);
        this.endDate = format.format(endDate);
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
