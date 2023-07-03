package org.supla.android.data.source;

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

import org.supla.android.data.source.local.ColorListDao;
import org.supla.android.db.ColorListItem;

public class DefaultColorListRepository implements ColorListRepository {

  private final ColorListDao colorListDao;

  public DefaultColorListRepository(ColorListDao colorListDao) {
    this.colorListDao = colorListDao;
  }

  @Override
  public ColorListItem getColorListItem(int id, boolean group, int idx) {
    return colorListDao.getColorListItem(id, group, idx);
  }

  @Override
  public void updateColorListItemValue(ColorListItem item) {
    ColorListItem cli =
        colorListDao.getColorListItem(item.getRemoteId(), item.getGroup(), item.getIdx());
    if (cli == null) {
      colorListDao.insert(item);
    } else {
      cli.AssignColorListItem(item);
      colorListDao.update(cli);
    }
  }
}
