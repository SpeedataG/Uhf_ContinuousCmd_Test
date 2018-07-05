package com.diankeyuandemo.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DataDb {
    @Id(autoincrement = true)
    private Long id;
    private String FactorNname;
    private String FactorNum;
    @Generated(hash = 203310431)
    public DataDb(Long id, String FactorNname, String FactorNum) {
        this.id = id;
        this.FactorNname = FactorNname;
        this.FactorNum = FactorNum;
    }
    @Generated(hash = 149697833)
    public DataDb() {
    }

    public DataDb(String factorNname, String factorNum) {
        FactorNname = factorNname;
        FactorNum = factorNum;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFactorNum() {
        return this.FactorNum;
    }
    public void setFactorNum(String FactorNum) {
        this.FactorNum = FactorNum;
    }
    public String getFactorNname() {
        return this.FactorNname;
    }
    public void setFactorNname(String FactorNname) {
        this.FactorNname = FactorNname;
    }

    


}
