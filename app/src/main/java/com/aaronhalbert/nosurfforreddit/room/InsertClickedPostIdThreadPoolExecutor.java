package com.aaronhalbert.nosurfforreddit.room;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* Use a ThreadPoolExecutor instead of a regular Thread to eliminate the overhead of creating
 * a new thread every time a post is clicked and a post ID is written to the database */
public class InsertClickedPostIdThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private final ThreadPoolExecutor threadPoolExecutor;

    public InsertClickedPostIdThreadPoolExecutor() {
        threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingQueue<>());
    }

    public void insertClickedPostId(ClickedPostIdDao clickedPostIdDao, ClickedPostId id) {
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        threadPoolExecutor.execute(runnable);
    }
}
