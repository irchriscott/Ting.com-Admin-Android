package com.codepipes.tingadmin.tableview.listener;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface ITableViewListener {

    void onCellClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int
            row);

    void onCellDoubleClicked(@NonNull RecyclerView.ViewHolder cellView, int column, int
            row);

    void onCellLongPressed(@NonNull RecyclerView.ViewHolder cellView, int column, int
            row);

    void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder columnHeaderView, int
            column);

    void onColumnHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder columnHeaderView, int
            column);

    void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder columnHeaderView, int
            column);

    void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder rowHeaderView, int row);

    void onRowHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder rowHeaderView, int row);

    void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder rowHeaderView, int
            row);

}
