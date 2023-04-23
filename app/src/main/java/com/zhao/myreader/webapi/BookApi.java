package com.zhao.myreader.webapi;

import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.common.URLCONST;
import com.zhao.myreader.enums.BookSource;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhao on 2017/7/24.
 */

public class BookApi {

    public static void getNewChapterCount(Book book, final ResultCallback callback){
        String[] urls=book.getChapterUrl().split("\\|");
        String indexUrl=urls[0];
        final int startPos;
        if(urls.length>1)
            startPos=Integer.parseInt(urls[1]);
        else
            startPos=0;

        if(BookSource.tianlai.toString().equals(book.getSource())) {
        	HttpUtil.httpGet_Async(indexUrl, null, "GBK", new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                	int maxChapterNo=TianLaiReadUtil.getMaxChapterNoFromHtml((String) o,book);
                	callback.onFinish(maxChapterNo,0);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        }else {
        	BookApi.getBookChapters(book, new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    final ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                    callback.onFinish(chapters.size(),0);
                }

                @Override
                public void onError(Exception e) {
                	callback.onError(e);
                }
            });
        }
        
    }
    
    public static void getBookChapters(Book book, final ResultCallback callback){
    	 String[] urls=book.getChapterUrl().split("\\|");
   		 String indexUrl=urls[0];
   		 final int startPos;
   		 if(urls.length>1)
   			 startPos=Integer.parseInt(urls[1]);
   		 else
   			 startPos=0;

		HttpUtil.httpGet_Async(indexUrl, null, "utf-8", new ResultCallback() {
   	            @Override
   	            public void onFinish(Object html, int code) {
   	            	List<Chapter> list=TianLaiReadUtil.getChaptersFromHtml((String) html,book);
   	            	if(startPos>0) {
   	            		for(Iterator<Chapter> itr=list.iterator();itr.hasNext();) {
   	   	            		Chapter chapter=itr.next();
   	   	            		chapter.setNumber(chapter.getNumber()+startPos);
   	   	            	}
   	            	}
   	                callback.onFinish(list,0);
   	                if(!indexUrl.equals(book.getChapterUrl().split("\\|")[0])) {
   	                	String newChapterUrl=book.getChapterUrl().split("\\|")[0]+"|"+(startPos+list.size());
   	                	book.setChapterUrl(newChapterUrl);
   	                	getBookChapters(book,callback);
   	                }
   	            }

   	            @Override
   	            public void onError(Exception e) {
   	                callback.onError(e);
   	            }
   	        });
     }


    public static void getChapterContent(String url, final ResultCallback callback){
        int tem = url.indexOf("\"");
        if (tem != -1){
            url = url.substring(0,tem);
        }
        if (!url.contains("http")){
            url = URLCONST.nameSpace_tianlai + url;
        }

		HttpUtil.httpGet_Async(url, null, "utf-8", new ResultCallback() {
            @Override
            public void onFinish(Object html, int code) {
            	final StringBuffer content=new StringBuffer();
            	content.append(TianLaiReadUtil.getContentFromHtml((String)html));
            	if(content.toString().equals("")) {
            		callback.onFinish("", 0);
            		return;
            	}
            		
            	String nextPage=TianLaiReadUtil.getNextPageFromHtml((String)html);
            	if(nextPage!=null) {
					HttpUtil.httpGet_Async(nextPage, null, "utf-8", new ResultCallback() {
                        public void onFinish(Object html, int code) {
                        	String page2=TianLaiReadUtil.getContentFromHtml((String)html);
                        	//start with"\0xa1 \0xa1"
                        	if(page2.length()>2)
                        		content.append(page2.substring(2));
                        	
                        	String nextPage=TianLaiReadUtil.getNextPageFromHtml((String)html);
                        	if(nextPage!=null) {
								HttpUtil.httpGet_Async(nextPage, null, "utf-8", new ResultCallback() {
									public void onFinish(Object html, int code) {
										String page3=TianLaiReadUtil.getContentFromHtml((String)html);
										if(page3.length()>2)
											content.append(page3.substring(2));
										callback.onFinish(content.toString(), 0);
									}
									public void onError(Exception e) {  
										callback.onError(e); 
									}
                        		});
                        	}else
                        		callback.onFinish(content.toString(),0);
                        }
                        public void onError(Exception e) { 
                        	callback.onError(e); 
                        }
                    });
            	}else
                	callback.onFinish(content.toString(),0);
            }
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    public static void search(String key, final ResultCallback callback){
    	Map<String,Object> params = new HashMap<>();
        params.put("keyword", key);
		HttpUtil.httpGet_Async(URLCONST.method_buxiu_search, params, "utf-8", new ResultCallback() {
            @Override
            public void onFinish(Object html, int code) {
            	callback.onFinish(TianLaiReadUtil.getBooksFromSearchHtml((String)html),code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


}
