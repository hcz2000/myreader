package com.zhao.myreader.ui.home.bbs;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.loader.content.AsyncTaskLoader;
import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.greendao.service.StockService;
import com.zhao.myreader.source.HttpDataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class StockLoader extends AsyncTaskLoader<List<Stock>>{
    private StockService mStockService;
    private ArrayList<Stock> mStocks = new ArrayList<>();

    StockLoader(Context context,ArrayList<Stock> stocks ) {
        super(context);
        mStocks=stocks;
        mStockService = new StockService();
    }
    @Override
    protected void onStartLoading(){
        forceLoad();
    }

    @Override
    public List<Stock> loadInBackground() {
        for(Stock stock : mStocks){
            String stockCode = stock.getId();
            String url;
            if (stockCode.startsWith("6")) {
                url = "https://xueqiu.com/s/SH" + stockCode;
            } else {
                url = "https://xueqiu.com/s/SZ" + stockCode;
            }
            HttpDataSource.httpGet_html(url, "utf-8", new ResultCallback() {
                @Override
                public void onFinish(Object content, int code) {
                    Document doc = Jsoup.parse((String) content);
                    Elements stockPrices = doc.getElementsByClass("stock-current");
                    if (!stockPrices.isEmpty()) {
                        Element stockPrice=stockPrices.first();
                        String priceStr = stockPrice.text();
                        Double price=Double.parseDouble(priceStr.substring(1));
                        stock.setPrice(price);
                        System.out.println(stock.getId()+":"+price);
                        mStockService.updateStock(stock);
                    }
                }

                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return mStocks;
    }
}
