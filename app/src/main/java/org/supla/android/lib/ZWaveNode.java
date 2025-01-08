package org.supla.android.lib;

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

public class ZWaveNode {
  private short NodeId;
  private short SceneType;
  private final int Flags;
  private Integer ChannelId;
  private String Name;

  public ZWaveNode(short nodeId, short sceneType, int flags, Integer channelId, String name) {
    NodeId = nodeId;
    SceneType = sceneType;
    Flags = flags;
    ChannelId = channelId;
    Name = name;
  }

  public short getNodeId() {
    return NodeId;
  }

  public void setNodeId(short nodeId) {
    NodeId = nodeId;
  }

  public short getSceneType() {
    return SceneType;
  }

  public void setSceneType(short sceneType) {
    SceneType = sceneType;
  }

  public int getFlags() {
    return Flags;
  }

  public Integer getChannelId() {
    return ChannelId;
  }

  public void setChannelId(Integer channelId) {
    ChannelId = channelId;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }
}
