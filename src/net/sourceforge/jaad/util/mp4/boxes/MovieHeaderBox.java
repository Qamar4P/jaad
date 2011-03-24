/*
 * Copyright (C) 2010 in-somnia
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
package net.sourceforge.jaad.util.mp4.boxes;

import net.sourceforge.jaad.util.mp4.FullBox;
import net.sourceforge.jaad.util.mp4.MP4InputStream;
import java.io.IOException;

public class MovieHeaderBox extends FullBox {

	private long timescale, duration;

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		if(version==1) {
			in.skipBytes(16);
			timescale = in.readBytes(4);
			duration = in.readBytes(8);
			left -= 28;
		}
		else {
			in.skipBytes(8);
			timescale = in.readBytes(4);
			duration = in.readBytes(4);
			left -= 16;
		}
	}

	public long getDuration() {
		return duration;
	}
}
