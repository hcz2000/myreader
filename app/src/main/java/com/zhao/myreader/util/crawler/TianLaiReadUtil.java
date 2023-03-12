package com.zhao.myreader.util.crawler;

import com.zhao.myreader.common.URLCONST;
import com.zhao.myreader.enums.BookSource;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.util.StringHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天籁小说网html解析工具
 * Created by zhao on 2017/7/24.
 */

public class TianLaiReadUtil {


    /**
     * 从html中获取章节正文
     *
     * @param html
     * @return
     */
    public static String getContentFromHtml(String html) {

        Document doc = Jsoup.parse(html);
        Element divContent = doc.getElementById("content");
        if(divContent==null)
            divContent = doc.getElementById("nr1");

        if (divContent != null) {
            //String content = Html.fromHtml(divContent.html()).toString();
        	//String content = divContent.toString();
        	String content = divContent.text();
            if(content.matches("^.+\\.23sk\\.[a-zA-Z]{3}努力更新ing.+$"))
        		return "";
        	content=content.replaceAll("(?i)m\\.23sk\\.c[o|0]m","");
        	content=content.replaceAll("天籁小说网","");
            content=content.replace(" ","\n");
            return content;
        } else {
            return "";
        }
    }
    
    /**
     * 从html中获取章节下页URL
     *
     * @param html
     * @return
     */
    public static String getNextPageFromHtml(String html) {

        Document doc = Jsoup.parse(html);
        Element td = doc.getElementsByClass("next").first();
 
        if (td != null) {
        	Element a=td.getElementsByTag("a").first();
        	if("下一页".equals(a.text())){
        		String url = a.attr("href");
    			return URLCONST.nameSpace_tianlai + url;
        	}
        } 
        return null;
    }
    
    /**
     * 从html中获取最新章节
     *
     * @param html
     * @return
     */
    public static int getMaxChapterNoFromHtml(String html,Book book) {
    	int maxChapterNo=book.getChapterTotalNum();
        Document doc = Jsoup.parse(html);
        if(BookSource.tianlai.toString().equals(book.getSource())){
            Element div = doc.getElementsByClass("listpage").first();
            Element span = div.getElementsByClass("middle").first();
            Element select=span.getElementsByTag("select").first();
            Element option=span.getElementsByTag("option").last();
            if(option!=null) {
            	String chapterDesc = option.text();
            	String lastChapter=chapterDesc.split(" - ")[1];
            	String regEx="[^0-9]";  
            	Pattern p = Pattern.compile(regEx);  
            	Matcher m = p.matcher(lastChapter);
            	maxChapterNo=Integer.parseInt(m.replaceAll("").trim());
            }
        }
        return maxChapterNo;
    }

    /**
     * 从html中获取章节列表
     *
     * @param html
     * @return
     */
    public static ArrayList<Chapter> getChaptersFromHtml(String html,Book book) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        if(BookSource.biquge.toString().equals(book.getSource())){
            Element divList = doc.getElementById("list");
            Element dl = divList.getElementsByTag("dl").get(0);

            String lastTile = null;
            int i = 0;
            for(Element dd : dl.getElementsByTag("dd")){
                Elements as = dd.getElementsByTag("a");
                if (as.size() > 0) {
                    Element a = as.get(0);
                    String title = a.html();
                    if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) {
                        continue;
                    }
                    Chapter chapter = new Chapter();
                    chapter.setNumber(i++);
                    chapter.setTitle(title);
                    String url = a.attr("href");
                    url = book.getChapterUrl() + url;
                    chapter.setUrl(url);
                    chapters.add(chapter);
                    lastTile = title;
                }

            }
        }else{
            Element ul = doc.getElementsByClass("chapter").last();

            String lastTile = null;
            
            int i = 0;
            for(Element dd : ul.getElementsByTag("li")) {
                Elements as = dd.getElementsByTag("a");
                if (as.size() > 0) {
                    Element a = as.get(0);
                    String title = a.html();
                    if (!StringHelper.isEmpty(lastTile) && title.equals(lastTile)) {
                        continue;
                    }
                    Chapter chapter = new Chapter();
                    chapter.setNumber(i++);
                    chapter.setTitle(title);
                    String url = a.attr("href");
                    url = URLCONST.nameSpace_tianlai + url;
                    chapter.setUrl(url);
                    chapters.add(chapter);
                    lastTile = title;
                }
            }
            
            Element div = doc.getElementsByClass("listpage").first();
            Element span = div.getElementsByTag("span").last();
            Element a=span.getElementsByAttribute("href").first();
            if(a!=null) {
            	String url = a.attr("href");
            	if(!url.startsWith("javascript"))
            		book.setChapterUrl(URLCONST.nameSpace_tianlai+url);
            }
          }
        return chapters;
    }
    /**
     * 从搜索html中得到书列表
     *
     * @param html
     * @return
     */
    public static ArrayList<Book> getBooksFromSearchHtml(String html) {
        ArrayList<Book> books = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements divs = doc.getElementsByClass("bookbox");
        for (Element element : divs) {
             Book book = new Book();
                Element img = element.child(0).child(0).child(0);
                book.setImgUrl(img.attr("src"));
                Element title = element.getElementsByClass("bookname").get(0);
                book.setName(title.child(0).child(0).text());
                book.setChapterUrl(URLCONST.nameSpace_tianlai + title.child(0).child(0).attr("href"));
                Element desc = element.getElementsByClass("intro_line").get(0);
                book.setDesc(desc.text());
                Element author = element.getElementsByClass("author").get(0);
                book.setAuthor(author.text());
                Element update = element.getElementsByClass("update").get(0);
                Element newChapter = update.child(1);
                book.setNewestChapterUrl(newChapter.attr("href"));
                book.setNewestChapterTitle(newChapter.text());
                book.setSource(BookSource.tianlai.toString());
                books.add(book);
        }
        return books;
    }

}
