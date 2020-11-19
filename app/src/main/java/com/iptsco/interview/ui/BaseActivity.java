package com.iptsco.interview.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.iptsco.interview.R;


/**
 * Parent of all AppCompatActivities
 */
public class BaseActivity extends AppCompatActivity {
    protected Activity mActivity;
    protected Context mContext;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Parent Variables
        mActivity = this;
        mContext = this;

        // Progress Dialog setup
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.base_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }

    /**
     * Handles the back button on toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows progress dialog
     */
    protected void showLoading() {
        if (mProgressDialog != null && !mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    /**
     * Hides already shown progress dialog
     */
    protected void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
