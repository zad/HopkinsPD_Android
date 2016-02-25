package edu.jhu.hopkinspd;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class ViewPagerAdapter extends FragmentPagerAdapter {
 
    // Declare the number of ViewPager pages
    final int PAGE_COUNT = 3;
 
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
 
        // Open FragmentTab1.java
        case 0:
            FragmentTabMonitor fragmenttab1 = new FragmentTabMonitor();
            return fragmenttab1;
 
        // Open FragmentTab2.java
        case 1:
            FragmentTabTest fragmenttab2 = new FragmentTabTest();
            return fragmenttab2;
 
        // Open FragmentTab3.java
        case 2:
            FragmentTabStats fragmenttab3 = new FragmentTabStats();
            return fragmenttab3;
        }
        return null;
    }
 
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
 
}