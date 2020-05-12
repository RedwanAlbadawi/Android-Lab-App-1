package com.example.vijaya.firebaseloginsignup.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

//  dialog manager class is used to show dialog to the user.
public class DialogManager{

    public static void showAlertDialog(final Context context, String title, String message, int icon,
                                       final Class<? extends Activity> activityToOpen){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(icon);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(activityToOpen == null){
                    dialog.dismiss();
                }
                else{
                    Intent intent = new Intent(context,activityToOpen);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }

            }
        });
        AlertDialog alertDialog =  builder.create();
        alertDialog.show();
    }

}
