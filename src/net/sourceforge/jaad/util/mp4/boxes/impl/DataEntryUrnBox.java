package net.sourceforge.jaad.util.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.util.mp4.MP4InputStream;
import net.sourceforge.jaad.util.mp4.boxes.FullBox;

public class DataEntryUrnBox extends FullBox {

	private String referenceName, location;

	public DataEntryUrnBox() {
		super("Data Entry Urn Box", "urn ");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		if((flags&1)==0) {
			referenceName = in.readUTFString((int) left, MP4InputStream.UTF8);
			left -= referenceName.length();
			if(left>0) {
				location = in.readUTFString((int) left, MP4InputStream.UTF8);
				left -= location.length();
			}
		}
	}

	public String getReferenceName() {
		return referenceName;
	}

	public String getLocation() {
		return location;
	}
}
