/*
 * YADI (Yet Another DLMS Implementation)
 * Copyright (C) 2018 Paulo Faco (paulofaco@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yadi.java.client.linklayer;

import yadi.java.client.phylayer.PhyLayer;
import yadi.java.client.phylayer.PhyLayerException;

public interface LinkLayer {
	public void connect(PhyLayer phy) throws PhyLayerException, LinkLayerException;
	public void disconnect(PhyLayer phy) throws PhyLayerException, LinkLayerException;
	public void send(PhyLayer phy, byte[] data) throws PhyLayerException, LinkLayerException;
	public byte[] read(PhyLayer phy) throws PhyLayerException, LinkLayerException;
}