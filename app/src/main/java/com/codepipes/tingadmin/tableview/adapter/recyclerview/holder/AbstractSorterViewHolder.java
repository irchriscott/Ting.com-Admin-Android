package com.codepipes.tingadmin.tableview.adapter.recyclerview.holder;

import androidx.annotation.NonNull;
import android.view.View;

import com.codepipes.tingadmin.tableview.sort.SortState;

public class AbstractSorterViewHolder extends AbstractViewHolder {
    @NonNull
    private SortState mSortState = SortState.UNSORTED;

    public AbstractSorterViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void onSortingStatusChanged(@NonNull SortState pSortState) {
        this.mSortState = pSortState;
    }

    @NonNull
    public SortState getSortState() {
        return mSortState;
    }
}
