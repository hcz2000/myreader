package com.zhan.myreader;

import java.util.ArrayList;
import java.util.List;

import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.entity.bookstore.BookType;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.webapi.BookStoreApi;

public class BookStoreTest {
	public static void main(String[] args) {
		BookStoreApi.getBookTypeList(URLCONST.nameSpace_tianlai+"/sort.html", (Object o, int code) -> {
			List<BookType> mBookTypes = (List<BookType>) o;
			BookType type = mBookTypes.get(0);
			getBookList(type);
		});
	}
	
	static private  void getBookList(BookType type) {
		BookStoreApi.getBookRankList(URLCONST.nameSpace_tianlai + type.getUrl(), (Object o, int code) -> {
			List<Book>bookList = (ArrayList<Book>) o;
			for(Book book: bookList) {
				System.out.println(book.getName()+":"+book.getChapterUrl());
			}
		});
	}
}