package net.sourceforge.jaad.mp4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.jaad.mp4.api.MetaData;
import net.sourceforge.jaad.mp4.api.MetaData.Field;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.boxes.Box;
import net.sourceforge.jaad.mp4.boxes.BoxFactory;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.impl.GoProTagsBox;
import net.sourceforge.jaad.mp4.boxes.impl.MovieHeaderBox;

public class GoProUtil {
	
	public static void main(String[] args) {
		ClassLoader classLoader = GoProUtil.class.getClassLoader();
//      File file = new File(classLoader.getResource("Hi-GOPR0489-1.mp4").getFile());
      File file = new File(classLoader.getResource("Tag-GOPR0476.MP4").getFile());
      try {
		getHilights(new MP4InputStream(new RandomAccessFile(file, "r")));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	public static long[] getHilights(MP4InputStream in) throws IOException {
		long[] hiLights = null;
		final List<Box> boxes = new ArrayList<Box>();
		//read all boxes
		Box box = null;
		long type;
		boolean moovFound = false;
		while(in.hasLeft()) {
			box = BoxFactory.parseBox(null, in);
			if(boxes.isEmpty()&&box.getType()!=BoxTypes.FILE_TYPE_BOX) throw new MP4Exception("no MP4 signature found");
			boxes.add(box);

			type = box.getType();
			if(type==BoxTypes.FILE_TYPE_BOX) {
				continue;
			}
			else if(type==BoxTypes.MOVIE_BOX) {
				Box moov = box;
				moovFound = true;
//				MetaData metaData = new MetaData();
//				
//				if(moov.hasChild(BoxTypes.META_BOX)) {
//					metaData.parse(null, moov.getChild(BoxTypes.META_BOX));
//					for (Object keyValue: metaData.getAll().values()) {
//						System.out.println(""+keyValue);
//					}
//				}
				System.out.println("getCreationTime"+new Movie(moov, in));
				if(moov.hasChild(BoxTypes.USER_DATA_BOX)) {
					final Box udta = moov.getChild(BoxTypes.USER_DATA_BOX);
					for (Object keyValue: udta.getChildren()) {
						System.out.println(""+keyValue);
					}
					//gopro Type: 1213025620, Offset: 272
					if(udta.hasChild(BoxTypes.GO_PRO_TAGS_BOX)) {
						final GoProTagsBox tags = (GoProTagsBox) udta.getChild(BoxTypes.GO_PRO_TAGS_BOX);
						System.out.println("Name: "+tags.getName()+", Type: "+tags.getType()+", Offset: "+ tags.getOffset());
						System.out.println("Tag Count: "+tags.getCount()+", HiLights: "+tags.getHiLights());
					}
					
				}
			}
			else if(type==BoxTypes.PROGRESSIVE_DOWNLOAD_INFORMATION_BOX) {
				;
			}
			else if(type==BoxTypes.MEDIA_DATA_BOX) {
				if(moovFound) break;
				else if(!in.hasRandomAccess()) throw new MP4Exception("movie box at end of file, need random access");
			}
		}

		return hiLights;
	}
	

}
