package ankushpathak1511962.com.teamninjaskillathon;

/**
 * Created by Ankush on 28-08-2017.
 */

public class Entry {
    String serialNo, name, phoneNo, email;

    public Entry(String serialNo, String name, String phoneNo, String email) {
        this.serialNo = serialNo;
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public Entry() {
        serialNo = "NA";
        name = "NA";
        phoneNo = "NA";
        email = "NA";
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
