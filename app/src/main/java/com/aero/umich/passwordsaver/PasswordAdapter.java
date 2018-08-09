package com.aero.umich.passwordsaver;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aero.umich.passwordsaver.database.PasswordContract;


public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    // create tag for log
    private static final String TAG = PasswordAdapter.class.getSimpleName();

    // The context we use for utility methods, app resources and layout inflaters
    private Context mContext;

    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


    // Class variables for the Cursor that holds task data and the Context
    private Cursor mCursor;

    // constructor
    public PasswordAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
    }


    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     */
    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.list_item_show, viewGroup, false);

        view.setFocusable(true);

        return new PasswordViewHolder(view);
    }


    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     */
    @Override
    public
    void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {

        // Indices for the _id, description, and priority columns
        int idIndex = mCursor.getColumnIndex(PasswordContract.PasswordEntry._ID);
        int IndexAccountInfo = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_ACCOUNTINFO);
        int IndexUserName = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_USERNAME);
        int IndexEmail = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_EMAIL);
        int IndexPassword = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_PASSWORD);
        int IndexSecurityLevel = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_SECURITY);
        int IndexDescription = mCursor.getColumnIndex(PasswordContract.PasswordEntry.COLUMN_DESCRIPTION);

        // get to the right location in the cursor
        mCursor.moveToPosition(position);

        /* Read date from the cursor */
        final int id = mCursor.getInt(idIndex);
        String accountInfo = mCursor.getString(IndexAccountInfo);
        String userName = mCursor.getString(IndexUserName);
        String emailAddress = mCursor.getString(IndexEmail);
        String password = mCursor.getString(IndexPassword);
        int    securityLevel = mCursor.getInt(IndexSecurityLevel);
        String description = mCursor.getString(IndexDescription);

        holder.bindView(id, accountInfo, userName, securityLevel);
    }


    // Returns the number of items to display.
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor newCursor) {

        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == newCursor) {
            return null; // bc nothing has changed
        }

        Cursor tempCursor = mCursor;
        mCursor = newCursor; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (newCursor != null) {
            notifyDataSetChanged();
        }

        return tempCursor;
    }


    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class PasswordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Views to show for each item in RecyclerView
        final TextView mAccountInfo;
        final TextView mUsername;
        final TextView mLevelSecurity;

        public PasswordViewHolder(View itemView) {
            super(itemView);

            mAccountInfo = (TextView) itemView.findViewById(R.id.tv_acount);
            mUsername = (TextView) itemView.findViewById(R.id.tv_username);
            mLevelSecurity = (TextView) itemView.findViewById(R.id.tv_level);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

//          COMPLETED (37) Instead of passing the String for the clicked item, pass the date from the cursor
            mCursor.moveToPosition(clickedPosition);

            int cursor_id = mCursor.getInt(mCursor.getColumnIndex(PasswordContract.PasswordEntry._ID));

            mOnClickListener.onListItemClick(cursor_id);
        }

        // helper method for bind view for each item in RecyclerView
        void bindView(int id, String accountInfo, String userName, int securityLevel) {

            //Set values
            itemView.setTag(id);

            // set text for all views
            mAccountInfo.setText(accountInfo);
            mUsername.setText(userName);
            mLevelSecurity.setText(Integer.toString(securityLevel));

            // set color and number for the security levels
            GradientDrawable securityCircle = (GradientDrawable) mLevelSecurity.getBackground();

            int securityColor = getSecurityLevelColor(securityLevel);
            securityCircle.setColor(securityColor);
        }
    }


    /**
     * Helper method for selecting the correct security level circle color.
     *  P1 = red, P2 = orange, P3 = yellow
     */
    private int getSecurityLevelColor(int securityLevel) {

        int securityColor = 0;

        // Get the appropriate background color based on the level
        switch(securityLevel) {
            case 1: securityColor = ContextCompat.getColor(mContext, R.color.materialRed);
                break;
            case 2: securityColor = ContextCompat.getColor(mContext, R.color.materialOrange);
                break;
            case 3: securityColor = ContextCompat.getColor(mContext, R.color.materialYellow);
                break;
            default: break;
        }
        return securityColor;
    }
}
