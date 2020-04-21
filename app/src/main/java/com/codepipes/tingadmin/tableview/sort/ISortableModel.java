package com.codepipes.tingadmin.tableview.sort;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ISortableModel {
    @NonNull
    String getId();

    @Nullable
    Object getContent();
}
