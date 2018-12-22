package com.kunfei.bookshelf.widget.recycler.refresh;

import com.kunfei.bookshelf.widget.recycler.refresh.BaseRefreshListener;

public interface OnRefreshWithProgressListener extends BaseRefreshListener {

    public int getMaxProgress();
}
