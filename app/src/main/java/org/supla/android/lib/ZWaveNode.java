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
    private String Name;
    private boolean Online;
    private short Errors;
    private short Value;
    private boolean EOL;

    public ZWaveNode(short nodeId, short sceneType, String name,
                     boolean online, short errors, short value, boolean EOL) {
        NodeId = nodeId;
        SceneType = sceneType;
        Name = name;
        Online = online;
        Errors = errors;
        Value = value;
        this.EOL = EOL;
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isOnline() {
        return Online;
    }

    public void setOnline(boolean online) {
        Online = online;
    }

    public short getErrors() {
        return Errors;
    }

    public void setErrors(short errors) {
        Errors = errors;
    }

    public short getValue() {
        return Value;
    }

    public void setValue(short value) {
        Value = value;
    }

    public boolean isEOL() {
        return EOL;
    }

    public void setEOL(boolean EOL) {
        this.EOL = EOL;
    }
}
