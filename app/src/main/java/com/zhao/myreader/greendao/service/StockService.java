package com.zhao.myreader.greendao.service;

import android.database.Cursor;

import com.zhao.myreader.greendao.GreenDaoManager;
import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.greendao.gen.StockDao;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhao on 2017/8/3.
 */

public class StockService extends BaseService {

    private ArrayList<Stock> findStocks(String sql, String[] selectionArgs) {
        ArrayList<Stock> stocks = new ArrayList<>();
        try {
            Cursor cursor = selectBySql(sql, selectionArgs);
            if (cursor != null){
                while (cursor.moveToNext()) {
                    Stock stock = new Stock();
                    stock.setId(cursor.getString(0));
                    stock.setName(cursor.getString(1));
                    stock.setQuantity(cursor.getInt(2));
                    stock.setCost(cursor.getDouble(3));
                    stock.setLastPrice(cursor.getDouble(4));
                    stock.setPrice(cursor.getDouble(5));
                    stock.setUpperThreshold(cursor.getDouble(6));
                    stock.setLowerThreshold(cursor.getDouble(7));
                    stock.setSortCode(cursor.getInt(8));
                    stocks.add(stock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stocks;
    }

    /**
     * 返回所有股票
     * @return
     */
    public ArrayList<Stock> findAllStocks() {
        String sql = "select * from stock order by sort_code";
        return findStocks(sql, null);
    }



    public void addStock(Stock stock) {
       addEntity(stock);
    }

    /**
     * 删除记录
     * @param stockId
     */
    public void deleteStockByID(String stockId){
        StockDao stockDao = GreenDaoManager.getInstance().getSession().getStockDao();
        stockDao.deleteByKey(stockId);
    }


    public Stock findStockByID(String stockid){
        Stock result = null;
        String sql = "select * from stock where id = ?";
        Cursor cursor = selectBySql(sql,new String[]{stockid});
        if (cursor !=null && cursor.moveToNext()){
            result = new Stock();
            result.setId(cursor.getString(0));
            result.setName(cursor.getString(1));
            result.setQuantity(cursor.getInt(2));
            result.setCost(cursor.getDouble(3));
            result.setLastPrice(cursor.getDouble(4));
            result.setPrice(cursor.getDouble(5));
            result.setUpperThreshold(cursor.getDouble(6));
            result.setLowerThreshold(cursor.getDouble(7));
            result.setSortCode(cursor.getInt(8));
        }
        return result;
    }


    /**
     * 添加或更新记录
     * @param stock
     */
    public void addOrUpdateStock(Stock stock){
        Stock stockInDB = findStockByID(stock.getId());
        if (stockInDB == null){
            addStock(stock);
        }else {
            updateEntity(stock);
        }
    }

    public void updateStock( Stock stock){
        updateEntity(stock);
     }

    public void updateStocks(List<Stock> stocks){
        StockDao stockDao = GreenDaoManager.getInstance().getSession().getStockDao();
        stockDao.updateInTx(stocks);
    }

}
