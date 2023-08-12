package com.zhan.myreader.webapi;

import android.util.Log;

import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.enums.BookSource;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.util.HttpUtil;
import com.zhan.myreader.util.StringHelper;
import com.zhan.myreader.util.crawler.TianlaiUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhan on 2017/7/24.
 */

public class BookApi {

    public static void getNewChapterCount(Book book, final ResultCallback callback){
        String[] urls=book.getChapterUrl().split("\\|");
        String indexUrl=urls[0];

        if(BookSource.tianlai.toString().equals(book.getSource())) {
        	HttpUtil.httpGet_Async(indexUrl, null, "GBK", new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                	int maxChapterNo= TianlaiUtil.getMaxChapterNoFromHtml((String) o,book);
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
                    final List<Chapter> chapters = (List<Chapter>) o;
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
		Log.d("BookApi","url:"+book.getChapterUrl()+" startPos:"+startPos);
		HttpUtil.httpGet_Async(indexUrl, null, "utf-8",	new ResultCallback() {
			@Override
			public void onFinish(Object html, int code) {
				List<Chapter> list= TianlaiUtil.getChaptersFromHtml((String) html,book,startPos);
   	            callback.onFinish(list,0);
   	            if(!indexUrl.equals(book.getChapterUrl().split("\\|")[0])) {
   	            	getBookChapters(book,callback);
   	            }
			}

			@Override
			public void onError(Exception e) {
   	            callback.onError(e);
   	        }
		});
     }


    public static void getChapterContent(Chapter chapter, final ResultCallback callback){
		String url=chapter.getUrl();

		HttpUtil.httpGet_Async(url, null, "utf-8", new ResultCallback() {
            @Override
            public void onFinish(Object html, int code) {
            	final StringBuffer content=new StringBuffer();
            	content.append(TianlaiUtil.getContentFromHtml((String)html));
            	if(content.toString().equals("")) {
            		callback.onFinish("", 0);
            		return;
            	}
            		
            	String nextPage= TianlaiUtil.getNextPageFromHtml((String)html);
            	if(nextPage!=null) {
					HttpUtil.httpGet_Async(nextPage, null, "utf-8", new ResultCallback() {
                        public void onFinish(Object html, int code) {
                        	String page2= TianlaiUtil.getContentFromHtml((String)html);
                        	//start with"\0xa1 \0xa1"
                        	if(page2.length()>2)
                        		content.append(page2.substring(2));
                        	
                        	String nextPage= TianlaiUtil.getNextPageFromHtml((String)html);
                        	if(nextPage!=null) {
								HttpUtil.httpGet_Async(nextPage, null, "utf-8", new ResultCallback() {
									public void onFinish(Object html, int code) {
										String page3= TianlaiUtil.getContentFromHtml((String)html);
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

	public static String getChapterContent(Chapter chapter){
		StringBuilder content=new StringBuilder();
		String page1Url = chapter.getUrl();
		String page1Html=HttpUtil.httpGet_Sync(page1Url);
		if(page1Html==null)
			return null;
		content.append(TianlaiUtil.getContentFromHtml(page1Html));
		if(content.toString().equals("")) {
			chapter.setContent("");
		}

		String page2Url= TianlaiUtil.getNextPageFromHtml(page1Html);
		if(page2Url!=null){
			String page2Html=HttpUtil.httpGet_Sync(page2Url);
			if(page2Html==null)
				return null;
			String page2= TianlaiUtil.getContentFromHtml(page2Html);
			String page3Url= TianlaiUtil.getNextPageFromHtml(page2Html);
			if(page2.length()>2) {
				content.append(page2.substring(2));
			}
			if(page3Url!=null){
				String page3Html=HttpUtil.httpGet_Sync(page3Url);
				if(page3Html==null)
					return null;
				String page3= TianlaiUtil.getContentFromHtml(page3Html);
				if(page3.length()>2)
					content.append(page3.substring(2));
			}
		}
		return content.toString();
	}

	public static void search(String key, final ResultCallback callback){
		if(StringHelper.isNumeric(key)){
			String bookUrl=URLCONST.bookprefix_tianlai+"/"+key.substring(0,key.length()-3)+"/"+key+"/";
			System.out.println(bookUrl);
			HttpUtil.httpGet_Async(bookUrl, null, "utf-8", new ResultCallback() {
				@Override
				public void onFinish(Object html, int code) {
					ArrayList<Book> result=new ArrayList();
					Book book=TianlaiUtil.getBookFromHtml(html.toString());
					if(book!=null)
						result.add(book);
					callback.onFinish(result, code);
				}

				@Override
				public void onError(Exception e) {
					callback.onError(e);
				}
			});
		}else {
			Map<String, Object> params = new HashMap<>();
			params.put("keyword", key);
			HttpUtil.httpGet_Async(URLCONST.method_buxiu_search, params, "utf-8", new ResultCallback() {
				@Override
				public void onFinish(Object html, int code) {
					callback.onFinish(TianlaiUtil.getBooksFromSearchHtml((String) html), code);
				}

				@Override
				public void onError(Exception e) {
					callback.onError(e);
				}
			});
		}
	}

}
