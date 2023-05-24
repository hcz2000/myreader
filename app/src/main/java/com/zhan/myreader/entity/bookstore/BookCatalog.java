package com.zhan.myreader.entity.bookstore;

/**
 * @author zhan
 * 书城小说分类
 * 2020/4/13 11:46
 */
public class BookCatalog {


    private String typeName;//分类名称
    private String url;//分类链接

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
