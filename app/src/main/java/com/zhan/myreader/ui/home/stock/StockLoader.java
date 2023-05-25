package com.zhan.myreader.ui.home.stock;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import com.zhan.myreader.greendao.entity.Stock;
import com.zhan.myreader.greendao.service.StockService;
import com.zhan.myreader.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class StockLoader extends AsyncTaskLoader<List<Stock>>{
    private StockService mStockService;
    private List<Stock> mStocks = new ArrayList<>();

    public StockLoader(Context context) {
        super(context);
        mStockService = new StockService();
    }
    @Override
    protected void onStartLoading(){
        //System.out.println("onStartLoading");
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Stock> loadInBackground() {
        //System.out.println("loadInBackground");
        mStocks=mStockService.findAllStocks();

        for(Stock stock : mStocks){
            String stockCode = stock.getId();
            String url;
            if (stockCode.startsWith("6")) {
                url = "https://xueqiu.com/s/SH" + stockCode;
            } else {
                url = "https://xueqiu.com/s/SZ" + stockCode;
            }
            String content= HttpUtil.httpGet_Sync(url);
            Document doc = Jsoup.parse((String) content);
            Elements stockPrices = doc.getElementsByClass("stock-current");
            if (!stockPrices.isEmpty()) {
                Element stockPrice=stockPrices.first();
                String priceStr = stockPrice.text();
                Double price=Double.parseDouble(priceStr.substring(1));
                stock.setPrice(price);
                //System.out.println(stock.getId()+":"+price);
                mStockService.updateStock(stock);
            }

        }
        return mStocks;
    }
    @Override
    protected void onStopLoading() {
        //System.out.println("onStopLoading");
        cancelLoad();
    }

    @Override
    public void deliverResult(List<Stock> data) {
        //System.out.println("deliverResult");
        if (isStarted()) {
            super.deliverResult(data);
        }
    }
}
