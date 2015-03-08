/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.liberate;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.navigationdrawerexample.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int m_size;
    private CharSequence mDrawerTitle;
    //private CharSequence mTitle;
    private String[] mPlanetTitles;
    ArrayList<Bitmap> lockedImages;
    ArrayList<Bitmap> liberatedImages;
    ArrayList<Bitmap> lockedImages_inv;
    ArrayList<Bitmap> liberatedImages_inv;
    Boolean solved;
    ImageAdapter m_im;
    int[][] cells;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlanetTitles = getResources().getStringArray(R.array.menu_item);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        m_size = 3;
        newGame(m_size);
        final Button button = (Button) findViewById(R.id.merge_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                for(int i = 0; i < m_size; i++)
                    for(int j = 0; j < m_size; j++)
                        if (cells[i][j] == 1)
                            cells[i][j] = 0;
                        else
                            cells[i][j] = 1;
                Solver sv = new Solver(cells);
                int ans[][] = sv.solve();
                solved = true;
                for(int i = 0; i < m_size; i++)
                    for(int j = 0; j < m_size; j++)
                    {
                        if (ans[i][j] == 1)
                        {
                            invert(j, i);
                        }
                    }
                m_im.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        return super.onOptionsItemSelected(item);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    private void Swap(int position)
    {
        if (position < 0) return;
        if (position >=  m_size * m_size) return;
        Bitmap temp = lockedImages.get(position);
        lockedImages.set(position, liberatedImages.get(position));
        liberatedImages.set(position, temp);
    }
    private void invert(int i, int j)
    {
        int position = i * m_size + j;
        if (cells[position % m_size][position / m_size] == 1) {
            lockedImages.set(position, liberatedImages_inv.get(position));
        }
        else {
            lockedImages.set(position, lockedImages_inv.get(position));
        }
    }
    private void newGame(int size) {
        ImageView image = new ImageView(getApplicationContext());
        image.setImageResource(R.drawable.locked);
        int chunkNumbers = size * size;
        cells = new int[size][size];
        image.setImageResource(R.drawable.locked);
        lockedImages = splitImage(image, chunkNumbers);
        image.setImageResource(R.drawable.locked_inv);
        lockedImages_inv = splitImage(image, chunkNumbers);
        image.setImageResource(R.drawable.liberated);
        liberatedImages = splitImage(image, chunkNumbers);
        image.setImageResource(R.drawable.liberated_inv);
        liberatedImages_inv = splitImage(image, chunkNumbers);
        final GridView grid = (GridView) findViewById(R.id.gridview);
        final ImageAdapter im = new ImageAdapter(this, lockedImages);
        grid.setAdapter(im);
        grid.setNumColumns(size);
        Random randomno = new Random();
        m_im = im;
        for(int i = 0; i < randomno.nextInt(m_size * m_size); i++)
        {
            int toggle = randomno.nextInt(m_size * m_size);
            Swap(toggle);
            if (cells[toggle % m_size][toggle / m_size] == 0)
                cells[toggle % m_size][toggle / m_size] = 1;
            else
                cells[toggle % m_size][toggle / m_size] = 0;
        }
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                TextView t=(TextView)findViewById(R.id.score);
                t.setText(String.valueOf(Integer.parseInt( t.getText().toString()) + 1));
                t=(TextView)findViewById(R.id.hintcount);
                t.setText(String.valueOf(Integer.parseInt(t.getText().toString()) + 1));
                int row = position / m_size;
                int col = position % m_size;

                Swap(position);
                if(col != 0) Swap(position - 1);
                if(col != m_size - 1) Swap(position + 1);
                Swap(position - m_size);
                Swap(position + m_size);
                im.notifyDataSetChanged();
            }
        });
    }
    private ArrayList<Bitmap> splitImage(ImageView image, int chunkNumbers) {

        //For the number of rows and columns of the grid to be displayed
        int rows, cols;
        rows = cols = 5;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Getting the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight()/rows;
        chunkWidth = bitmap.getWidth()/cols;

        //xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for(int x=0; x<rows; x++){
            int xCoord = 0;
            for(int y=0; y<cols; y++){
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        return chunkedImages;
    }

}