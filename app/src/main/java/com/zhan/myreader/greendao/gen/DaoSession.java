package com.zhan.myreader.greendao.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.zhan.myreader.greendao.entity.Stock;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.SearchHistory;
import com.zhan.myreader.greendao.entity.Chapter;

import com.zhan.myreader.greendao.gen.StockDao;
import com.zhan.myreader.greendao.gen.BookDao;
import com.zhan.myreader.greendao.gen.SearchHistoryDao;
import com.zhan.myreader.greendao.gen.ChapterDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig stockDaoConfig;
    private final DaoConfig bookDaoConfig;
    private final DaoConfig searchHistoryDaoConfig;
    private final DaoConfig chapterDaoConfig;

    private final StockDao stockDao;
    private final BookDao bookDao;
    private final SearchHistoryDao searchHistoryDao;
    private final ChapterDao chapterDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        stockDaoConfig = daoConfigMap.get(StockDao.class).clone();
        stockDaoConfig.initIdentityScope(type);

        bookDaoConfig = daoConfigMap.get(BookDao.class).clone();
        bookDaoConfig.initIdentityScope(type);

        searchHistoryDaoConfig = daoConfigMap.get(SearchHistoryDao.class).clone();
        searchHistoryDaoConfig.initIdentityScope(type);

        chapterDaoConfig = daoConfigMap.get(ChapterDao.class).clone();
        chapterDaoConfig.initIdentityScope(type);

        stockDao = new StockDao(stockDaoConfig, this);
        bookDao = new BookDao(bookDaoConfig, this);
        searchHistoryDao = new SearchHistoryDao(searchHistoryDaoConfig, this);
        chapterDao = new ChapterDao(chapterDaoConfig, this);

        registerDao(Stock.class, stockDao);
        registerDao(Book.class, bookDao);
        registerDao(SearchHistory.class, searchHistoryDao);
        registerDao(Chapter.class, chapterDao);
    }
    
    public void clear() {
        stockDaoConfig.clearIdentityScope();
        bookDaoConfig.clearIdentityScope();
        searchHistoryDaoConfig.clearIdentityScope();
        chapterDaoConfig.clearIdentityScope();
    }

    public StockDao getStockDao() {
        return stockDao;
    }

    public BookDao getBookDao() {
        return bookDao;
    }

    public SearchHistoryDao getSearchHistoryDao() {
        return searchHistoryDao;
    }

    public ChapterDao getChapterDao() {
        return chapterDao;
    }

}
