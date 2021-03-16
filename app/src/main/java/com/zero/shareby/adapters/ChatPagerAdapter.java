package com.zero.shareby.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.zero.shareby.chats.GroupChatFragment;
import com.zero.shareby.chats.RecentChats;


public class ChatPagerAdapter extends FragmentStatePagerAdapter {

    public ChatPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new GroupChatFragment();

            case 1:
                return new RecentChats();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Group Chat";

            case 1:
                return "Recent Chats";

            default:
                return "default";
        }
    }
}
