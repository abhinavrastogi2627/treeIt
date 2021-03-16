package com.zero.shareby.Utils;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class DialogBuilder {
    public static AlertDialog.Builder getDialogBuilder(Context context, String title, String message){
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }
}
