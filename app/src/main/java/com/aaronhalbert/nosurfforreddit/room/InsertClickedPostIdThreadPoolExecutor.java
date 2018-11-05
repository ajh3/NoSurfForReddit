package com.aaronhalbert.nosurfforreddit.room;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InsertClickedPostIdThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final ClickedPostIdDao clickedPostIdDao;
    private final ThreadPoolExecutor threadPoolExecutor;

    private static InsertClickedPostIdThreadPoolExecutor instance;

    private InsertClickedPostIdThreadPoolExecutor(ClickedPostIdDao clickedPostIdDao) {
        threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingQueue<>());

        this.clickedPostIdDao = clickedPostIdDao;
    }

    public void insertClickedPostId(ClickedPostId id) {
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        threadPoolExecutor.execute(runnable);
    }

    public static InsertClickedPostIdThreadPoolExecutor getInstance(ClickedPostIdDao clickedPostIdDao) {
        if (instance == null) {
            instance = new InsertClickedPostIdThreadPoolExecutor(clickedPostIdDao);
        }

        return instance;
    }
}
