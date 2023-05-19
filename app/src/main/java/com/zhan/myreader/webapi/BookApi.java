package com.zhan.myreader.webapi;

import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.enums.BookSource;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.util.HttpUtil;
import com.zhan.myreader.util.crawler.TianLaiReadUtil;
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
		HttpUtil.httpGet_Async(indexUrl, null, "utf-8",	new ResultCallback() {
			@Override
			public void onFinish(Object html, int code) {
				List<Chapter> list=TianLaiReadUtil.getChaptersFromHtml((String) html,book);
   	            if(startPos>0) {
					for(Chapter chapter: list) {
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


    public static void getChapterContent(Chapter chapter, final ResultCallback callback){
		String url=chapter.getUrl();

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

	public static String getChapterContent(Chapter chapter){
		StringBuilder content=new StringBuilder();
		String page1Url = chapter.getUrl();
		String page1Html=HttpUtil.httpGet_Sync(page1Url);
		if(page1Html==null)
			return null;
		content.append(TianLaiReadUtil.getContentFromHtml(page1Html));
		if(content.toString().equals("")) {
			chapter.setContent("");
		}

		String page2Url=TianLaiReadUtil.getNextPageFromHtml(page1Html);
		if(page2Url!=null){
			String page2Html=HttpUtil.httpGet_Sync(page2Url);
			if(page2Html==null)
				return null;
			String page2=TianLaiReadUtil.getContentFromHtml(page2Html);
			String page3Url=TianLaiReadUtil.getNextPageFromHtml(page2Html);
			if(page2.length()>2) {
				content.append(page2.substring(2));
			}
			if(page3Url!=null){
				String page3Html=HttpUtil.httpGet_Sync(page3Url);
				if(page3Html==null)
					return null;
				String page3=TianLaiReadUtil.getContentFromHtml(page3Html);
				if(page3.length()>2)
					content.append(page3.substring(2));
			}
		}
		return content.toString();
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
