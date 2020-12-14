package org.supla.android.listview;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.SuplaContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListViewCursorAdapter extends BaseAdapter implements SectionLayout.OnSectionLayoutTouchListener {

    public static final int TYPE_CHANNEL = 0;
    public static final int TYPE_SECTION = 1;
    private static final int REORDERING_MODE_NOT_ACTIVE = -1;
    private Context context;
    private Cursor cursor;
    private ArrayList<SectionItem> Sections;
    private int currentSectionIndex;
    private boolean Group;
    private SectionLayout.OnSectionLayoutTouchListener onSectionLayoutTouchListener;

    private Map<Integer, Item> positionToItemMapping;
    private int emptyPosition = REORDERING_MODE_NOT_ACTIVE;
    private int selectedItem = REORDERING_MODE_NOT_ACTIVE;

    public ListViewCursorAdapter(Context context, Cursor cursor) {
        super();
        init(context, cursor);
    }

    public ListViewCursorAdapter(Context context, Cursor cursor, boolean group) {
        super();
        init(context, cursor);
        Group = group;
    }

    private void init(Context context, Cursor cursor) {
        currentSectionIndex = 0;
        Sections = new ArrayList<>();
        setCursor(cursor);
        this.context = context;
    }

    private boolean SectionsDiff(ArrayList<SectionItem> S1, ArrayList<SectionItem> S2) {

        if (S1.size() != S2.size())
            return true;

        for (int a = 0; a < S1.size(); a++) {

            SectionItem s1 = S1.get(a);
            SectionItem s2 = S2.get(a);

            if (s1.getPosition() != s2.getPosition()
                    || s1.getCollapsed() != s2.getCollapsed()
                    || !s1.getCaption().equals(s2.getCaption()))
                return true;
        }

        return false;
    }

    @Override
    public int getCount() {
        if (cursor == null || cursor.isClosed()) {
            return 0;
        }

        return cursor.getCount() + Sections.size();
    }

    @Override
    public Object getItem(int position) {

        if (Sections.size() == 0) {
            cursor.moveToPosition(position);
            return cursor;
        }

        SectionItem section = Sections.get(currentSectionIndex);

        if (section.getPosition() == position) {
            return section;
        }

        if (currentSectionIndex == 0
                && position < section.getPosition()) {

            cursor.moveToPosition(position);
            return cursor;
        }

        if (position > section.getPosition()
                && currentSectionIndex == Sections.size() - 1) {

            cursor.moveToPosition(position - currentSectionIndex - 1);
            return cursor;
        }

        int start = currentSectionIndex + 1;
        int size = Sections.size();

        for (int n = 0; n < 2; n++) {

            for (int a = start; a < size; a++) {

                section = Sections.get(a);

                if (section.getPosition() == position) {
                    return section;
                }

                if (position > section.getPosition()
                        && (a == size - 1
                        || position < Sections.get(a + 1).getPosition())) {

                    currentSectionIndex = a;

                    cursor.moveToPosition(position - a - 1);
                    return cursor;
                }
            }

            start = 0;
        }


        return null;
    }

    public int getSectionIndexAtPosition(int position) {

        if (Sections.size() == 0) {
            return -1;
        }

        SectionItem section = Sections.get(currentSectionIndex);

        if (section.getPosition() == position) {
            return currentSectionIndex;
        }

        int size = Sections.size();

        for (int a = 0; a < size; a++) {

            section = Sections.get(a);

            if (section.getPosition() == position) {
                return a;
            }

            if (position > section.getPosition()
                    && (a == size - 1
                    || position < Sections.get(a + 1).getPosition())) {

                return a;
            }
        }

        return -1;

    }

    public SectionItem getSectionAtIndex(int idx) {

        if (idx < 0
                || idx >= Sections.size())
            return null;

        return Sections.get(idx);
    }

    public SectionItem getSectionAtPosition(int position) {

        return getSectionAtIndex(getSectionIndexAtPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof SectionItem) {
            return TYPE_SECTION;
        }

        return TYPE_CHANNEL;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object obj = null;
        if (emptyPosition == -1) {
            obj = getItem(position);
        } else {
            if (emptyPosition < selectedItem) {
                if (position < emptyPosition || position > selectedItem) {
                    obj = getItem(position);
                }
                else if (position > emptyPosition) {
                    obj = getItem(position - 1);
                }
            } else if (emptyPosition > selectedItem) {
                if (position < selectedItem || position > emptyPosition) {
                    obj = getItem(position);
                } else if (position >= selectedItem && position < emptyPosition) {
                    obj = getItem(position + 1);
                }
            } else {
                if (position != emptyPosition) {
                    obj = getItem(position);
                }
            }
        }

        int _collapsed;

        if (isGroup()) {
            _collapsed = 0x2;
        } else {
            _collapsed = 0x1;
        }

        if (obj instanceof SectionItem) {

            if (((SectionItem) obj).view == null) {
                ((SectionItem) obj).view = new SectionLayout(context);
                ((SectionItem) obj).view.setCaption(((SectionItem) obj).getCaption());
                ((SectionItem) obj).view.setLocationId(((SectionItem) obj).getLocationId());
                ((SectionItem) obj).view.setOnSectionLayoutTouchListener(this);
                ((SectionItem) obj).view.
                        setCollapsed((((SectionItem) obj).getCollapsed() & _collapsed) > 0);
            }

            convertView = ((SectionItem) obj).view;

        } else if (obj instanceof Cursor) {

            ChannelBase cbase;

            if (isGroup()) {
                cbase = new ChannelGroup();
            } else {
                cbase = new Channel();
            }

            int collapsed = cursor.getInt(cursor.getColumnIndex("collapsed"));
            if ((collapsed & _collapsed) > 0) {

                if (!(convertView instanceof View)
                        || convertView.getVisibility() != View.GONE) {
                    convertView = new View(context);
                    convertView.setVisibility(View.GONE);
                }

                return convertView;
            }

            if (!(convertView instanceof ChannelLayout)
                    || convertView.getVisibility() == View.GONE) {
                convertView = new ChannelLayout(context, parent instanceof ChannelListView ? (ChannelListView) parent : null);
            }

            cbase.AssignCursorData((Cursor) obj);
            SectionItem channelSection = getSectionAtPosition(position);
            setData((ChannelLayout) convertView, cbase, channelSection);
        } else {
            ChannelLayout channelLayout = new ChannelLayout(context, parent instanceof ChannelListView ? (ChannelListView) parent : null);
            convertView = channelLayout;
        }

        return convertView;
    }

    public void setData(ChannelLayout channelLayout, ChannelBase cbase, SectionItem channelSection) {
        channelLayout.setChannelData(cbase);
    }

    public Cursor getCursor() {
        return this.cursor;
    }

    private void setCursor(Cursor cursor) {

        currentSectionIndex = 0;
        positionToItemMapping = new HashMap<>();

        ArrayList<SectionItem> Sections = new ArrayList<>();

        if (cursor != null
                && !cursor.isClosed()) {

            String caption = null;
            int position = 0;
            int positionInLocation = 1;
            int channelIdColumnIndex = cursor.getColumnIndex(SuplaContract.ChannelEntry._ID);
            int sectionColumnIndex = cursor.getColumnIndex("section");
            int locationIdColumnIndex = cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_LOCATIONID);
            int collapsedColumnIndex = cursor.getColumnIndex(SuplaContract.LocationEntry.COLUMN_NAME_COLLAPSED);

            if (cursor.moveToFirst())
                do {

                    if (caption == null
                            || !cursor.getString(sectionColumnIndex).equals(caption)) {

                        caption = cursor.getString(sectionColumnIndex);
                        Sections.add(
                                new SectionItem(Sections.size() + cursor.getPosition(),
                                        cursor.getInt(locationIdColumnIndex),
                                        cursor.getInt(collapsedColumnIndex),
                                        caption)
                        );
                        positionInLocation = 1;
                        position++;
                    }
                    Item item = new Item(cursor.getLong(channelIdColumnIndex), cursor.getInt(locationIdColumnIndex), positionInLocation++);
                    positionToItemMapping.put(position++, item);
                } while (cursor.moveToNext());

        }

        if (this.Sections == null) {

            this.Sections = Sections;

        } else {

            if (SectionsDiff(this.Sections, Sections)) {
                this.Sections.clear();
                this.Sections = Sections;
            }


        }

        this.cursor = cursor;
    }

    public void changeCursor(Cursor cursor) {

        if (this.cursor != null) {
            this.cursor.close();
        }

        setCursor(cursor);
        if (cursor == null) {
            notifyDataSetInvalidated();
        } else {
            notifyDataSetChanged();
        }
    }

    public boolean isGroup() {
        return Group;
    }

    @Override
    public void onSectionLayoutTouch(Object sender, String caption, int locationId) {
        if (onSectionLayoutTouchListener != null) {
            onSectionLayoutTouchListener.onSectionLayoutTouch(this, caption, locationId);
        }
    }

    public void setOnSectionLayoutTouchListener(SectionLayout.OnSectionLayoutTouchListener listener) {
        onSectionLayoutTouchListener = listener;
    }

    public class SectionItem {
        private int locationId;
        private int collapsed;
        private String caption;
        private int position;
        private SectionLayout view;

        public SectionItem(int position, int locationId, int collapsed, String caption) {
            this.position = position;
            this.locationId = locationId;
            this.collapsed = collapsed;
            this.caption = caption;
        }

        public int getPosition() {
            return position;
        }

        public String getCaption() {
            return caption;
        }

        public int getLocationId() {
            return locationId;
        }

        public int getCollapsed() {
            return collapsed;
        }

        public SectionLayout getView() {
            return view;
        }

        public void setView(SectionLayout view) {
            this.view = view;
        }

    }

    public void updateReorderingMode(int selectedItem, int emptyPosition) {
        boolean invalidate = emptyPosition != this.emptyPosition;

        if (invalidate) {
            this.selectedItem = selectedItem;
            this.emptyPosition = emptyPosition;
            notifyDataSetChanged();
        }
    }

    public void stopReorderingMode() {
        this.selectedItem = REORDERING_MODE_NOT_ACTIVE;
        this.emptyPosition = REORDERING_MODE_NOT_ACTIVE;

        notifyDataSetChanged();
    }

    public boolean isReorderPossible(int initialPosition, int finalPosition) {
        Item initialPositionItem = positionToItemMapping.get(initialPosition);
        Item finalPositionItem = positionToItemMapping.get(finalPosition);
        if (initialPositionItem == null || finalPositionItem == null) {
            return false;
        }
        return initialPositionItem.locationId == finalPositionItem.locationId;
    }

    public Item getItemForPosition(int position) {
        return positionToItemMapping.get(position);
    }

    public class Item {
        public final long id;
        public final int locationId;
        public final int positionInLocation;

        public Item(long id, int locationId, int positionInLocation) {
            this.id = id;
            this.locationId = locationId;
            this.positionInLocation = positionInLocation;
        }
    }
}
