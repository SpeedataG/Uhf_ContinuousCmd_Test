package com.diankeyuandemo.util;

import com.diankeyuandemo.App;
import com.diankeyuandemo.db.DataDb;
import com.diankeyuandemo.db.DataDbDao;

import java.util.List;

/**
 * ----------Dragon be here!----------/
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 *
 * @author :孙天伟 in  2017/9/27   13:32.
 *         联系方式:QQ:420401567
 *         功能描述:  数据库增删改查
 */
public class DBUitl {
    public DBUitl() {
    }

    private DataDbDao mDao = App.getsInstance().getDaoSession().getDataDbDao();

    /**
     * 添加一条数据
     *
     * @param body
     */
    public void insertDtata(DataDb body) {
        mDao.insertOrReplace(body);
    }

    public void delete(String Num) {
        DataDb user = mDao.queryBuilder().where(DataDbDao.Properties.FactorNum.eq(Num)).build().unique();
        if (user != null) {
            mDao.deleteByKey(user.getId());
        }
    }

    /**
     * 查数据
     */
    public boolean queryNum(String Num) {
        DataDb user = mDao.queryBuilder().where(DataDbDao.Properties.FactorNum.eq(Num)).build().unique();
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }
//

    /**
     * 根据 体温标签编号 查找整条数据
     *
     * @return
     */
    public DataDb queryDbBody(String Num) {
        DataDb user = mDao.queryBuilder().where(DataDbDao.Properties.FactorNum.eq(Num)).build().unique();
        return user;
    }

    /**
     * 查找所有数据
     *
     * @return
     */
    public List<DataDb> queryAll() {
        List<DataDb> twBodies = mDao.loadAll();
        if (twBodies != null && twBodies.size() > 0)
            return twBodies;
        return twBodies;
    }

    /**
     * 修改指定数据
     */
    public void cahageData(String name, String num ,String oldNum) {

        DataDb user = mDao.queryBuilder().where(
                DataDbDao.Properties.FactorNum.eq(oldNum)).build().unique();
        if (user != null) {
            user.setFactorNname(name);
            user.setFactorNum(num);
            mDao.update(user);
        }
    }

}
